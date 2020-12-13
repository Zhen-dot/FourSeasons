package com.hep88

import com.hep88.model.Food.food
import com.hep88.model.{Customer, Order}
import com.hep88.protocol.{JsonSerializable, UPnP}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType.{Error, Information, Warning}


object ClientActor {

  sealed trait Command extends JsonSerializable

  private case class ListingResponse(listing: Receptionist.Listing) extends Command

  // Client-Actor Protocols
  case class Login(hp: String, password: String) extends Command

  case class Register(hp: String, name: String, password: String) extends Command

  case class Checkout(order: String) extends Command

  case class UpdateOrders(hp: String) extends Command

  case class Leave(customer: String) extends Command

  // Server-Client Procotols
  case class Foods(foods: String) extends Command

  case class ClientLoginAuth(customer: Option[String]) extends Command

  case class ClientRegistrationAuth(customer: Option[String]) extends Command

  case class ClientOrders(orders: String) extends Command

  case class ClientOrderStatus(status: Boolean) extends Command

  var defaultBehaviour: Option[Behavior[ClientActor.Command]] = None
  var remoteOpt: Option[ActorRef[ServerActor.Command]] = None

  def apply(): Behavior[ClientActor.Command] = {
    Behaviors.setup { ctx =>
      UPnP.bindPort(ctx)

      val listingAdapter: ActorRef[Receptionist.Listing] = ctx.messageAdapter { listing =>
        ClientActor.ListingResponse(listing)
      }

      ctx.system.receptionist ! Receptionist.subscribe(ServerActor.ServerKey, listingAdapter)

      defaultBehaviour = Some(Behaviors.receiveMessage {
        case ListingResponse(ServerActor.ServerKey.Listing(listings)) =>
          val xs: Set[ActorRef[ServerActor.Command]] = listings
          for (x <- xs) {
            remoteOpt = Some(x)
          }
          remoteOpt match {
            case Some(_) =>
              remoteOpt.foreach(_ ! ServerActor.GetFoods(ctx.self)); sessionStart()
            case None => println("No remote yet"); Behaviors.same
          }

        case o =>
          println("UNEXPECTED BEHAVIOUR")
          println(o)
          Behaviors.unhandled
      })
      defaultBehaviour.get
    }
  }

  def sessionStart(): Behavior[ClientActor.Command] = Behaviors.receive[ClientActor.Command] {
    (ctx, msg) =>
      msg match {
        case Foods(foods) =>
          Client.foods = foods.split("!").map(food)
          Behaviors.same

        case Login(hp, password) =>
          remoteOpt.foreach(_ ! ServerActor.LoginAuth(hp, password, ctx.self))
          Behaviors.same

        case Register(hp, name, password) =>
          remoteOpt.foreach(_ ! ServerActor.RegisterAuth(hp, name, password, ctx.self))
          Behaviors.same

        case Checkout(order) =>
          remoteOpt.foreach(_ ! ServerActor.PlaceOrder(order, ctx.self))
          Behaviors.same

        case UpdateOrders(hp) =>
          remoteOpt.foreach(_ ! ServerActor.GetOrders(hp, ctx.self))
          Behaviors.same

        case ClientLoginAuth(customer) =>
          customer match {
            case Some(c) =>
              if (c != "") {
                Client.user = Customer.customer(c)
                Platform.runLater {
                  Client.alert(Information, "Login Successful", s"Welcome, ${Client.user.name}")
                  Client.showOrdersPage()
                }
              } else {
                Platform.runLater {
                  Client.alert(Error, "Login Failed", "Login failed! You are already logged in.")
                }
              }
            case None =>
              Platform.runLater {
                Client.alert(Error, "Login Failed", "Login failed! Incorrect Phone Number or Password.\nPlease try again.")
              }
          }
          Behaviors.same

        case ClientRegistrationAuth(customer) =>
          customer match {
            case Some(c) =>
              Client.user = Customer.customer(c)
              Platform.runLater {
                Client.alert(Information, "Success", s"Registration Successful! Welcome, ${Client.user.name}")
                Client.showOrdersPage()
              }
            case None => Platform.runLater {
              Client.alert(Warning, "Number already registered", "Login or Register with a different number")
            }
          }
          Behaviors.same

        case ClientOrders(orders) =>
          Platform.runLater {
            if (orders != "") {
              Client.orders.clear()
              Client.orders ++= ObservableBuffer(orders.split("/").map(o => Order.order(o, Client.foods)): _*)
            }
          }
          Behaviors.same

        case ClientOrderStatus(status) =>
          if (status) {
            remoteOpt.foreach(_ ! ServerActor.GetOrders(Client.user.hp, ctx.self))
            Platform.runLater {
              Client.alert(Information, "Success", "Order placed successfully.")
              Client.showOrdersPage()
            }
          } else Platform.runLater {
            Client.alert(Error, "Order Failed", "Please try again")
          }
          Behaviors.same

        case Leave(customer) =>
          remoteOpt.foreach(_ ! ServerActor.EndSession(customer))
          Behaviors.same
      }
  }.receiveSignal {
    case (ctx, PostStop) =>
      try {
        remoteOpt.foreach(_ ! ServerActor.EndSession(Client.user.toString))
      }
      defaultBehaviour.getOrElse(Behaviors.same)
  }
}
