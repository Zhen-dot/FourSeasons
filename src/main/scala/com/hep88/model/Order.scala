package com.hep88.model

import com.hep88.util.DateUtil._
import java.time.LocalDateTime
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

object Order {
  def order(string: String, foods: Array[Food]): Order = {
    val params = string.split(";")
    val items = params(1).split(",").map(f => foods.filter(_.id == f).head)
    new Order(params(0), items, params(2).toDouble, params(3).asDate, params(4).toInt)
  }

  def apply(customerHp: String, items: String, amt: Double, dt: String, id: Int, foods: Array[Food]): Order = {
    val order = items.split(",").map(i => foods.filter(_.id == i).head)
    new Order(customerHp, order, amt, dt.asDate, id)
  }

}

case class Order(customerI: String,
            itemsI: Array[Food],
            amtI: Double,
            dtI: LocalDateTime = LocalDateTime.now,
            idI: Int = 0) {

  // Properties
  private val _customer: StringProperty = StringProperty(customerI)
  private val _items: ObservableBuffer[Food] = ObservableBuffer[Food](itemsI)
  private val _amt: ObjectProperty[Double] = ObjectProperty(amtI)
  private val _dt: ObjectProperty[LocalDateTime] = ObjectProperty(dtI)
  private val _id: ObjectProperty[Int] = ObjectProperty(idI)

  override def toString: String = s"$customer;${items.map(_.id).mkString(",")};$amt;${dt.asString};$id"

  // Accessors and Mutators
  def customer: String = _customer.get()

  def customerProperty: StringProperty = _customer

  def items: Array[Food] = _items.toArray

  def itemsProperty: ObservableBuffer[Food] = _items

  def itemsToString: String = {
    var string: String = ""
    for (i <- items.groupBy(identity).mapValues(_.length)) string += "%d\t %s\n".format(i._2, i._1.name)
    string
  }

  def amt: Double = _amt.get()

  def amtProperty: ObjectProperty[Double] = _amt

  def dt: LocalDateTime = _dt.get()

  def dtProperty: ObjectProperty[LocalDateTime] = _dt

  def id: Int = _id.get()

  def idProperty: ObjectProperty[Int] = _id
}