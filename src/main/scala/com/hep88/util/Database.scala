package com.hep88.util

import com.hep88.ClientActor.Foods
import com.hep88.model._
import com.hep88.util.DateUtil.DateFormatter
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import java.sql.DriverManager
import scalikejdbc._

import scala.util.{Failure, Success, Try}

trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"

  val dbURL = "jdbc:derby:FourSeasons;create=true;"
  val dbURLdestroy = "jdbc:derby:FourSeasons;shutdown=true;"
  Class.forName(derbyDriverClassname)

  ConnectionPool.singleton(dbURL, "FourSeasonsAdmin", "net")

  implicit val session: AutoSession.type = AutoSession

  //  System.setSecurityManager(null)
}

object Database extends Database {

  // Customer methods
  def login(hp: String, password: String): ObjectProperty[Customer] = {
    ObjectProperty(DB readOnly { implicit session =>
      sql"select * from customer where phone_num = $hp and password = $password".map(rs => Customer(
        rs.string("phone_num"),
        rs.string("name"),
        rs.string("password"))).single.apply().get
    })
  }

  def exist(customer: Customer): Boolean = {
    DB readOnly { implicit session =>
      sql"""
          select * from customer where
          phone_num = ${customer.hp}
     """.map(rs => rs.string("phone_num")).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }
  }

  def register(customer: Customer): Boolean = {
    if (!exist(customer)) {
      Try(DB autoCommit { implicit session =>
        sql"""
            insert into customer(phone_num, name, password) values
            (${customer.hp}, ${customer.name}, ${customer.password})
           """.updateAndReturnGeneratedKey.apply()
      })
      true
    } else {
      false
    }
  }

  def customers(): ObservableBuffer[Customer] = {
    ObservableBuffer(
      DB readOnly { implicit session =>
        sql"select * from customer".map(rs => Customer(
          rs.string("phone_num"),
          rs.string("name"),
          rs.string("password"))).list.apply()
      })
  }

  def customers(hp: String): ObjectProperty[Customer] = {
    ObjectProperty(DB readOnly { implicit session =>
      sql"select * from customer where phone_num = $hp".map(rs => Customer(
        rs.string("phone_num"),
        rs.string("name"),
        rs.string("password"))).single.apply().get
    })
  }

  // Food methods
  def foods(): ObservableBuffer[Food] = {
    ObservableBuffer(
      DB readOnly { implicit session =>
        sql"select * from food".map(rs => Food(
          rs.string("id"),
          rs.string("name"),
          rs.double("price"),
          rs.string("image"),
          rs.string("des"))).list.apply()
      })
  }

  def foods(id: String): ObjectProperty[Food] = {
    ObjectProperty(
      DB readOnly { implicit session =>
        sql"select * from food where id = $id".map(rs => Food(
          rs.string("id"),
          rs.string("name"),
          rs.double("price"),
          rs.string("image"),
          rs.string("des"))).single.apply().get
      })
  }

  def exist(food: Food): Boolean = {
    DB readOnly { implicit session =>
      sql"""
           select * from food where
           id = ${food.id}
         """.map(rs => rs.string("id")).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }
  }

  def add(food: Food): Try[Long] = {
    if (!exist(food)) {
      Try(DB autoCommit { implicit session =>
        sql"""
             insert into food(id, name, price, image, des) values
             (${food.id}, ${food.name}, ${food.price}, ${food.imageURL}, ${food.des})
           """.updateAndReturnGeneratedKey.apply()
      })
    } else {
      throw new Exception("Food Addition Failed")
    }
  }

  // Order methods
  def orders(hp: String, foods: Array[Food]): ObservableBuffer[Order] = {
    ObservableBuffer(
      DB readOnly { implicit session =>
        sql"select * from orders where customer_hp = $hp".map(rs => Order(
          rs.string("customer_hp"),
          rs.string("items"),
          rs.double("amt"),
          rs.string("dt"),
          rs.int("id"),
          foods),
        ).list.apply()
      })
  }

  def orders(foods: Array[Food]): ObservableBuffer[Order] = {
    ObservableBuffer(
      DB readOnly { implicit session =>
        sql"select * from orders".map(rs => Order(
          rs.string("customer_hp"),
          rs.string("items"),
          rs.double("amt"),
          rs.string("dt"),
          rs.int("id"),
          foods)).list.apply()
      })
  }

  def orders(id: Int, foods: Array[Food]): ObjectProperty[Order] = {
    ObjectProperty(
      DB readOnly { implicit session =>
        sql"select * from orders where id = $id".map(rs => Order(
          rs.string("customer_hp"),
          rs.string("items"),
          rs.double("amt"),
          rs.string("dt"),
          rs.int("id"),
          foods)).single.apply().get
      })
  }

  def add(order: Order): Try[Long] = {
    Try(DB autoCommit { implicit session =>
      sql"""
           insert into orders(customer_hp, items, amt, dt) values
           (${order.customer}, ${order.items.map(_.id).mkString(",")}, ${order.amt}, ${order.dt.asString})
         """.updateAndReturnGeneratedKey.apply()
    })
  }

  def setup(): Unit = {
    println("Database setting up...")
    // Customer table. Stores Hotel's Customer information.
    // No default initialisation.
    if ((DB getTable "customer").isEmpty) {
      DB autoCommit { implicit session =>
        sql"""
          create table customer(
          phone_num varchar(11) primary key,
          name varchar(64),
          password varchar(20))
         """.execute.apply()
      }
    }

    // Food table. Stores Hotel's Food information.
    // Default initialisation generates a basic menu of 10 items.
    if ((DB getTable "food").isEmpty) {
      DB autoCommit { implicit session =>
        sql"""
             create table food(
             id varchar(4) primary key,
             name varchar(32),
             price double,
             image varchar(255),
             des varchar(255))
           """.execute.apply()
      }
      add(new Food("th01", "Pad Krapow Basil Chicken Rice", 13.9, "1163322634.jpg",
        "Pad Kra Pow Basil Chicken with Steamed White Rice.\nComes with an egg omelette and boiled vegetables."))
      add(new Food("th02", "Tom Yam Chicken Noodle Soup", 13.9, "1163322618.jpg",
        "Noodles cooked in clear Tom Yam chicken broth."))
      add(new Food("bg01", "DC Special Burger", 13.9, "1072417.jpg",
        "Hotel's signature special burger with beef."))
      add(new Food("br01", "Breakfast Supreme", 15.9, "932823.jpg",
        "Scambled eggs, sausage, fried mushrooms, garlic bread and salad."))
      add(new Food("sd01", "Egg Mayo Sandwich", 13.9, "923523.jpg",
        "Classic eggs mayo served with raisins and cucumber slices in Swiss crossaint, served with chips and fresh salad."))
      add(new Food("sd02", "Tuna Sandwich", 14.9, "923524.jpg",
        "Tuna served with sunflower seeds and cucumber slices in Swiss croissant, served with mixed salad and chips."))
      add(new Food("sd03", "Steak and Cheese sandwich", 16.9, "923522.jpg",
        "Australian beef with onions, capsicums, and mozzarella cheese in Swiss croissant, served with chips and fresh salad."))
      add(new Food("ps01", "Meatballs Spaghetti", 16.9, "923516.jpg",
        "Classic tomato sauce with juicy beef meatball and Parmesan cheese"))
      add(new Food("ps02", "Chicken Bolognese Spaghetti", 16.9, "1175570173.jpg",
        "Signature bolognese sauce on pasta with strips of pan fried chicken breast"))
    }


    // Order table. Stores Orders requested by Hotel's Customers.
    // No default initialisation.
    if ((DB getTable "orders").isEmpty) {
      DB autoCommit { implicit session =>
        sql"""
             create table orders(
             id int generated always as identity primary key,
             dt varchar(64),
             customer_hp varchar(11) references customer(phone_num),
             items varchar(512),
             amt double)
           """.execute.apply()
      }
    }

  }

  def shutdown(): Unit = {
    println("Database shutting down...")
    Try(DriverManager.getConnection(dbURLdestroy)) match {
      case Success(_) => println("Database shutdown success")
      case Failure(_) => println("Database shutdown failed")
    }
  }

  def clear(): Unit = {
    if ((DB getTable "orders").isDefined) {
      DB autoCommit { implicit session =>
        sql"drop table orders".execute.apply()
      }
    }

    if ((DB getTable "food").isDefined) {
      DB autoCommit { implicit session =>
        sql"drop table food".execute.apply()
      }
    }

    if ((DB getTable "customer").isDefined) {
      DB autoCommit { implicit session =>
        sql"drop table customer".execute.apply()
      }
    }
  }
}

