package com.hep88.view

import com.hep88.{Client, ClientActor}
import scalafx.scene.control.Alert.AlertType.Confirmation
import scalafx.scene.control.{Button, ButtonType, PasswordField, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class CustomerLoginController(private val phoneNumField: TextField,
                              private val passwordField: PasswordField,
                              private val registerButton: Button,
                              private val loginButton: Button) {

  def login(): Unit = {
    Client.ref ! ClientActor.Login(phoneNumField.text.value, passwordField.text.value)
  }

  def register(): Unit = {
    Client.alert(Confirmation, "Register", "Go to Register?").get
    match {
      case ButtonType.OK =>
        Client.showCustomerRegister()
      case _ =>
    }
  }
}
