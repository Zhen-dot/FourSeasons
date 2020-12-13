package com.hep88.view

import com.hep88.{Client, ClientActor}
import com.hep88.model.{Food, Order}
import scalafx.Includes._
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert.AlertType.{Confirmation, Warning}
import scalafx.scene.control._
import scalafx.scene.image.ImageView
import javafx.scene.image.Image
import scalafxml.core.macros.sfxml

@sfxml
class OrderDialogController(private val nameLabel: Label,
                            private val priceLabel: Label,
                            private val totalLabel: Label,
                            private val foodImageView: ImageView,
                            private val addButton: Button,
                            private val incButton: Button,
                            private val decButton: Button,
                            private val backButton: Button,
                            private val checkoutButton: Button,
                            private val trashButton: ToggleButton,
                            private val descriptionTextArea: TextArea,
                            private val menuTable: TableView[Food],
                            private val menuItemColumn: TableColumn[Food, String],
                            private val menuPriceColumn: TableColumn[Food, String],
                            private val cartTable: TableView[Food],
                            private val cartItemColumn: TableColumn[Food, String],
                            private val cartAmountColumn: TableColumn[Food, Int],
                            private val cartTotalColumn: TableColumn[Food, String]) {

  // Initialise.
  display(None)
  totalLabel.text.value = 0.00.toString

  // Trashes items and resets ToggleButton on press.
  trashButton.setOnAction { _ => trashItem(); trashButton.setSelected(false) }

  // Menu Table properties.
  menuTable.items = ObservableBuffer(Client.foods.sortBy(_.id): _*)
  menuItemColumn.cellValueFactory = {
    _.value.nameProperty
  }
  menuPriceColumn.cellValueFactory = { i =>
    StringProperty("%.2f".format(i.value.price))
  }

  // Cart Table properties.
  private val cart: ObservableBuffer[Food] = new ObservableBuffer[Food]()

  // Refreshes cart when an item is added or removed.
  cart.onChange {
    try {
      val items = cart.groupBy(identity).mapValues(_.size)
      cartTable.getColumns.get(0).setVisible(false)
      cartTable.getColumns.get(0).setVisible(true)
      cartTable.items.setValue(ObservableBuffer(items.keys.toList))
      totalLabel.text.value = "%.2f".format(items.map { i =>
        i._2 * i._1.price
      }.sum)
    } catch {
      case _: Exception =>
    }
  }

  cartTable.items = ObservableBuffer(cart.groupBy(identity).mapValues(_.size).keys.toList)
  cartItemColumn.cellValueFactory = {
    _.value.nameProperty
  }
  cartAmountColumn.cellValueFactory = { i =>
    ObjectProperty(cart.groupBy(identity).mapValues(_.size)(i.value))
  }
  cartTotalColumn.cellValueFactory = { i =>
    StringProperty("%.2f".format(cart.groupBy(identity).mapValues(_.size)(i.value) * i.value.price))
  }

  // Details Selector
  menuTable.selectionModel().selectedItem.onChange {
    (_, _, value) => display(Some(value))
  }
  cartTable.selectionModel().selectedItem.onChange {
    (_, _, value) => display(Some(value))
  }

  private def display(food: Option[Food]): Unit = {
    try {
      food match {
        case Some(food) =>
          nameLabel.text = food.name
          priceLabel.text = "MYR\t%.2f".format(food.price)
          foodImageView.setImage(new Image(getClass.getClassLoader.getResource("com/hep88/images/" + food.imageURL).toString))
          descriptionTextArea.text = food.des
        case None =>
          nameLabel.text = ""
          priceLabel.text = ""
          descriptionTextArea.text = ""
      }
    } catch {
      case _: Exception =>
    }
  }

  def addToCart(): Unit = {
    try {
      menuTable.selectionModel().getSelectedItem.id
      cart += menuTable.selectionModel().getSelectedItem
    } catch {
      case e: Exception =>
        println(e)
        Client.alert(Warning, "No item selected", "Please select an item to add")
    }
  }

  def incItem(): Unit = {
    try {
      cartTable.selectionModel().getSelectedItem.id
      cart += cartTable.selectionModel().getSelectedItem
    } catch {
      case e: Exception =>
        println(e)
        Client.alert(Warning, "No item selected", "Please select an item to add")
    }
  }

  def decItem(): Unit = {
    try {
      cartTable.selectionModel().getSelectedItem.id
      cart -= cartTable.selectionModel().getSelectedItem
    } catch {
      case e: Exception =>
        println(e)
        Client.alert(Warning, "No item selected", "Please select an item to remove")
    }
  }

  def trashItem(): Unit = {
    try {
      cartTable.selectionModel().getSelectedItem.id
      cart --= cart.filter(_.name == cartTable.selectionModel().getSelectedItem.name)
    } catch {
      case e: Exception =>
        println(e)
        Client.alert(Warning, "No item selected", "Please select an item to remove")
    }
  }

  def checkout(): Unit = {
    try {
      if (cart.isEmpty) throw new Exception
      Client.ref ! ClientActor.Checkout(new Order(Client.user.hp, cart.toArray, totalLabel.text.value.toDouble).toString)
    } catch {
      case _: Exception =>
        Client.alert(Warning, "Empty Cart", "No item selected!")
    }
  }

  def back(): Unit = {
    Client.alert(Confirmation, "Return?", "Cancel order and return to orders?")
      .get match {
      case ButtonType.OK =>
        Client.showOrdersPage()
      case _ =>
    }
  }

}


