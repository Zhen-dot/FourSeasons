package com.hep88

import com.hep88.protocol.UPnP
import com.hep88.protocol.JsonSerializable
import com.hep88.model.{Customer, Food, Order}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import com.hep88.util.Database
import scalafx.collections.ObservableBuffer

object ServerActor {

  sealed trait Command extends JsonSerializable

  // Protocols
  case class GetFoods(sender: ActorRef[ClientActor.Command]) extends Command

  case class LoginAuth(hp: String, pass: String, sender: ActorRef[ClientActor.Command]) extends Command

  case class RegisterAuth(hp: String, name: String, pass: String, sender: ActorRef[ClientActor.Command]) extends Command

  case class GetOrders(hp: String, sender: ActorRef[ClientActor.Command]) extends Command

  case class PlaceOrder(order: String, sender: ActorRef[ClientActor.Command]) extends Command

  case class EndSession(customer: String) extends Command

  val ServerKey: ServiceKey[ServerActor.Command] = ServiceKey("ServerActor")

  val Session: ObservableBuffer[Customer] = new ObservableBuffer[Customer]()

  Session.onChange(println("\nSession changes detected.\n" + Session.mkString("\n")))

  def apply(): Behavior[ServerActor.Command] = Behaviors.setup { ctx =>
    ctx.system.receptionist ! Receptionist.Register(ServerKey, ctx.self)
    UPnP.bindPort(ctx)

    Behaviors.receiveMessage {
      case GetFoods(sender) =>
        sender ! ClientActor.Foods(Database.foods().map(f => f.toString).mkString("!"))
        Behaviors.same

      case LoginAuth(hp, pass, sender) =>
        try {
          val customer = Database.login(hp, pass).get
          if (Session.contains(customer)) sender ! ClientActor.ClientLoginAuth(Some(""))
          else {
            Session += customer
            sender ! ClientActor.ClientLoginAuth(Some(customer.toString))
          }
        } catch {
          case _: NoSuchElementException =>
            sender ! ClientActor.ClientLoginAuth(None)
        }
        Behaviors.same

      case RegisterAuth(hp, name, pass, sender) =>
        val customer = new Customer(hp, name, pass)
        if (Database.register(customer)) {
          Session += customer
          sender ! ClientActor.ClientRegistrationAuth(Some(customer.toString))
        } else {
          sender ! ClientActor.ClientRegistrationAuth(None)
        }
        Behaviors.same

      case GetOrders(hp, sender) =>
        sender ! ClientActor.ClientOrders(Database.orders(hp, Database.foods().toArray).map(o => o.toString).mkString("/"))
        Behaviors.same

      case PlaceOrder(order, sender) =>
        try {
          Database.add(Order.order(order, Database.foods().toArray))
          sender ! ClientActor.ClientOrderStatus(true)
        } catch {
          case _: Exception => sender ! ClientActor.ClientOrderStatus(false)
        }
        Behaviors.same

      case EndSession(customer) =>
        Session -= Customer.customer(customer)
        Behaviors.same
    }
  }
}
