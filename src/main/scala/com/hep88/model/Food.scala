package com.hep88.model

import scalafx.beans.property.{ObjectProperty, StringProperty}

object Food {
  def food(string: String): Food = {
    val params = string.split(";")
    new Food(params(0), params(1), params(2).toDouble, params(3), params(4))
  }

  def apply(id: String, name: String, price: Double, imageURL: String, des: String): Food = {
    new Food(id, name, price, imageURL, des)
  }
}

case class Food(idI: String,
                nameI: String,
                priceI: Double,
                imageURLI: String,
                desI: String) {

  // Properties
  private val _id: StringProperty = StringProperty(idI)
  private val _name: StringProperty = StringProperty(nameI)
  private val _price: ObjectProperty[Double] = ObjectProperty(priceI)
  private val _imageURL: StringProperty = StringProperty(imageURLI)
  private val _des: StringProperty = StringProperty(desI)

  override def toString: String = s"$id;$name;$price;$imageURL;$des"

  // Accessors and Mutators
  def id: String = _id.get()
  def idProperty: StringProperty = _id

  def name: String = _name.get()
  def nameProperty: StringProperty = _name

  def price: Double = _price.get()
  def priceProperty: ObjectProperty[Double] = _price

  def imageURL: String = _imageURL.get()
  def imageURLProperty: StringProperty = _imageURL

  def des: String = _des.get()
  def desProperty: StringProperty = _des

}

