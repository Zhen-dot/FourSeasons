package com.hep88.view

import com.hep88.{Client, ClientActor}
import com.hep88.model.Order
import com.hep88.util.DateUtil.{DateFormatter, orderingLocalDateTime}
import scalafx.Includes._
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType.{Confirmation, Information}
import scalafx.scene.control._
import scalafxml.core.macros.sfxml

@sfxml
class OrdersPageController(private val dateLabel: Label,
                           private val detailsLabel: Label,
                           private val detailsTextArea: TextArea,
                           private val logoutButton: Button,
                           private val orderButton: Button,
                           private val serviceTable: TableView[Order],
                           private val serviceTypeColumn: TableColumn[Order, String],
                           private val serviceDateColumn: TableColumn[Order, String]) {

  // Initialise.
  display(None)

  Client.orders.onChange {
    serviceTable.items = Client.orders.sortBy(_.dt)
    serviceTable.getColumns.get(0).setVisible(false)
    serviceTable.getColumns.get(0).setVisible(true)
    serviceTable.items = Client.orders.sortBy(_.dt)
  }

  // Service Table properties.
  serviceTable.items = Client.orders.sortBy(_.dt)

  serviceTypeColumn.cellValueFactory = {d =>
    StringProperty(d.value.amt.toString)
  }
  serviceDateColumn.cellValueFactory = { d =>
    StringProperty(d.value.dt.asString)
  }

  // Changes detailed display when a service is selected in serviceTable.
  serviceTable.selectionModel().selectedItem.onChange {
    (_, _, value) => display(Some(value))
  }

  private def display(order: Option[Order]): Unit = {
    order match {
      case Some(order) =>
        dateLabel.text = order.dt.asString
        detailsTextArea.text = order.itemsToString
        detailsLabel.text.value = order.amt.toString
      case None =>
        dateLabel.text = ""
        detailsTextArea.text = ""
    }
  }

  def toOrder(): Unit = {
    Client.showOrderDialog()
  }

  def logout(): Unit = {
    Client.alert(Confirmation, "Sign out", "Are you sure you want to sign out?").get
    match {
      case ButtonType.OK =>
        Client.ref ! ClientActor.Leave(Client.user.toString)
        Client.user = null
        Client.orders = new ObservableBuffer[Order]()
        Client.alert(Information, "Sign out", "Signed out.\nReturning to Login page.")
        Client.showCustomerLogin()
      case _ => Client.showOrdersPage()
    }
  }
}
