package com.hep88.view

import com.hep88.{Client, ClientActor}
import scalafx.scene.control.Alert.AlertType.{Confirmation, Error}
import scalafx.scene.control.{Button, ButtonType, PasswordField, TextField}
import scalafxml.core.macros.sfxml


@sfxml
class CustomerRegisterController(private val hpField: TextField,
                                 private val nameField: TextField,
                                 private val passwordField: PasswordField,
                                 private val rePasswordField: PasswordField,
                                 private val loginButton: Button,
                                 private val registerButton: Button) {

  private def nullCheck(i: String): Boolean = i == null || i.isEmpty

  def register(): Unit = {
    // Checks Input. Creates a new Customer object and registers it.
    if (checkInput()) {
      Client.ref ! ClientActor.Register(hpField.text.value, nameField.text.value, passwordField.text.value)
    }
  }

  def checkInput(): Boolean = {
    // String to store error message.
    var error: String = ""

    // Checks for Empty name.
    if (nullCheck(nameField.text.value))
      error += "Name empty!\n"

    // Checks for Empty Phone Number.
    if (nullCheck(hpField.text.value))
      error += "Phone Number Empty!\n"
    // Matches phone number to format 01#-####### or 01#-########.
    else if ("""^(\+?6?01)[0-46-9]-*[0-9]{7,8}$""".r.findAllMatchIn(hpField.text.value).isEmpty)
      error += "Please enter a valid Phone Number!\n"
    // Removes all non-numerical chars from Phone Number.
    else hpField.text.value = hpField.text.value.replaceAll("[^0-9]", "")

    // Checks for Empty Password.
    if (nullCheck(passwordField.text.value))
      error += "Password Empty!\n"
    // Checks if Password length < 8.
    else if (passwordField.text.value.length < 8)
      error += "Password too short!\n"

    // Checks for Empty RePassword.
    if (nullCheck(rePasswordField.text.value))
      error += "Password Reentry Empty!\n"
    // Checks if Password & RePassword match.
    else if (passwordField.text.value != rePasswordField.text.value)
      error += "Passwords do not match!\n"

    // Return true if no errors detected
    if (error.isEmpty) return true

    // Runs if errors detected & returns false
    Client.alert(Error, "Input errors detected", error)
    false
  }

  def login(): Unit = {
    Client.alert(Confirmation, "Login", "Go to Login?").get
    match {
      case ButtonType.OK =>
        Client.showCustomerLogin()
      case _ =>
    }
  }
}
