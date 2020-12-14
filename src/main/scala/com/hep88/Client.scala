package com.hep88

import com.hep88.protocol.FSConfig
import com.hep88.model.{Customer, Food, Order}
import com.typesafe.config.{Config, ConfigFactory}
import akka.cluster.typed._
import akka.discovery.{Discovery, Lookup, ServiceDiscovery}
import akka.discovery.ServiceDiscovery.Resolved
import akka.actor.typed.ActorRef
import akka.actor.{ActorSystem, Address}
import akka.actor.typed.scaladsl.adapter._
import scalafx.collections.ObservableBuffer

import scala.concurrent.Future
import scala.concurrent.duration._

// javafx packages
import javafx.scene.layout.BorderPane
import javafx.{scene => jfxs}

// scalafx packages
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.scene.image.Image
import scalafxml.core.{FXMLLoader, NoDependencyResolver => ndResolver}

object Client extends JFXApp {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val config: Config = ConfigFactory.load()
//  val mainSystem: ActorSystem = ActorSystem("MainSystem", FSConfig.askForConfigClient().withFallback(config))
  val mainSystem: ActorSystem = ActorSystem("MainSystem", FSConfig.askDevConfigClient().withFallback(config))
  val cluster: Cluster = Cluster(mainSystem.toTyped)
  val discovery: ServiceDiscovery = Discovery(mainSystem).discovery
  val ref: ActorRef[ClientActor.Command] = mainSystem.spawn(ClientActor(), "ClientActor")

  joinLocalSeedNode()
//  joinPublicSeedNode()

  // Initialise.
  private val loader = new FXMLLoader(getClass.getResource("view/RootLayout.fxml"), ndResolver)
  loader.load()
  private val roots: BorderPane = loader.getRoot[jfxs.layout.BorderPane]

  stage = new PrimaryStage {
    title = "Four Seasons"
    scene = new Scene {
      root = roots
    }
    icons += new Image(getClass.getResourceAsStream("images/logo.png"))
  }

  stage.onCloseRequest = handle({
    mainSystem.terminate()
  })

  var user: Customer = _
  var foods: Array[Food] = _
  var orders: ObservableBuffer[Order] = new ObservableBuffer[Order]()

  // Main
  showCustomerLogin()

  def alert(alert: Alert.AlertType, header: String, content: String): Option[ButtonType] = {
    new Alert(alert) {
      initOwner(Client.stage)
      title = alert.toString
      headerText = header
      contentText = content
    }.showAndWait()
  }

  def showCustomerRegister(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("view/CustomerRegister.fxml"), ndResolver)
    loader.load()
    roots.setCenter(loader.getRoot[jfxs.layout.AnchorPane])
  }

  def showCustomerLogin(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("view/CustomerLogin.fxml"), ndResolver)
    loader.load()
    roots.setCenter(loader.getRoot[jfxs.layout.AnchorPane])
  }

  def showOrdersPage(): Unit = {
    ref ! ClientActor.UpdateOrders(user.hp)
    val loader = new FXMLLoader(getClass.getResource("view/OrdersPage.fxml"), ndResolver)
    loader.load()
    roots.setCenter(loader.getRoot[jfxs.layout.AnchorPane])
  }

  def showOrderDialog(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("view/OrderDialog.fxml"), ndResolver)
    loader.load()
    roots.setCenter(loader.getRoot[jfxs.layout.AnchorPane])
  }

  def joinPublicSeedNode(): Unit = {
    val address = akka.actor.Address("akka", "MainSystem", "175.143.213.102", FSConfig.port)
    cluster.manager ! JoinSeedNodes(List(address))
  }

  def joinLocalSeedNode(): Unit = {
    val address = Address("akka", "MainSystem", FSConfig.localAddress.get.getHostAddress, FSConfig.port)
    cluster.manager ! JoinSeedNodes(List(address))
  }
}
