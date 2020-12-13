package com.hep88.model

import scalafx.beans.property._


object Customer {

  def customer(string: String): Customer = {
    val params = string.split(";")
    new Customer(params(0), params(1), params(2))
  }

  def apply(hp: String, name: String, password: String): Customer = {
    new Customer(hp, name, password)
  }

}

case class Customer(hpI: String,
               nameI: String,
               passwordI: String) {

  // Properties
  private val _hp: StringProperty = StringProperty(hpI)
  private val _name: StringProperty = StringProperty(nameI)
  private val _password: StringProperty = StringProperty(passwordI)

  override def toString: String = s"$hp;$name;$password"

  // Accessors and Mutators
  def hp: String = _hp.get()

  def hpProperty: StringProperty = _hp

  def name: String = _name.get()

  def nameProperty: StringProperty = _name

  def password: String = _password.get()

  def passwordProperty: StringProperty = _password
}

