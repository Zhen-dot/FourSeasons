package com.hep88.protocol

import java.net.InetAddress
import com.typesafe.config.{Config, ConfigFactory}

object FSConfig {

  var localAddress: Option[InetAddress] = None
  var runLocalOnly: Option[Boolean] = None
  var port: Int = 2222

  def askForConfig(): Config = {
    val ip = Array(192, 168, 1, 78)   // TODO Client LOCAL IP address
    val inetAddress = InetAddress.getByAddress(ip.map(x => x.toByte))
    localAddress = Some(inetAddress)
    runLocalOnly = Some(true)
    println("Please enter port to bind")
    val pip = "42.188.85.22"        // TODO Client PUBLIC IP address
    FSConfig(pip, localAddress.get.getHostAddress, 0.toString)
  }

  def askDevConfig(): Config = {
    val ip = Array(192, 168, 0, 159)
    val inetAddress = InetAddress.getByAddress(ip.map(x => x.toByte))
    localAddress = Some(inetAddress)
    runLocalOnly = Some(true)
    println("Please enter port to bind")
//    port = scala.io.StdIn.readLine.toInt
    port = 2222
    FSConfig(localAddress.get.getHostAddress, "", port.toString)
  }

  def askDevConfigClient(): Config = {
    val ip = Array(192, 168, 0, 159)
    val inetAddress = InetAddress.getByAddress(ip.map(x => x.toByte))
    localAddress = Some(inetAddress)
    runLocalOnly = Some(true)
    FSConfig(localAddress.get.getHostAddress, "", 0.toString)
  }

  def apply(extHostName: String, intHostName: String, port: String): Config = {
    ConfigFactory.parseString(
      s"""
         |akka {
         |  loglevel = "INFO" #INFO, DEBUG
         |  actor {
         |    # provider=remote is possible, but prefer cluster
         |    provider =  cluster
         |    allow-java-serialization=on
         |    serializers {
         |      jackson-json = "akka.serialization.jackson.JacksonJsonSerializer"
         |    }
         |    serialization-bindings {
         |      "com.hep88.protocol.JsonSerializable" = jackson-json
         |    }
         |  }
         |  remote {
         |    artery {
         |      transport = tcp # See Selecting a transport below
         |      canonical.hostname = "${extHostName}"
         |      canonical.port = ${port}
         |      bind.hostname = "${intHostName}" # internal (bind) hostname
         |      bind.port = ${port}              # internal (bind) port
         |
         |      #log-sent-messages = on
         |      #log-received-messages = on
         |    }
         |  }
         |  cluster {
         |    downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
         |  }
         |
         |  discovery {
         |    loglevel = "OFF"
         |    method = akka-dns
         |  }
         |
         |  management {
         |    loglevel = "OFF"
         |    http {
         |      hostname = "${extHostName}"
         |      port = 8558
         |      bind-hostname = "${intHostName}"
         |      bind-port = 8558
         |    }
         |  }
         |}
         """.stripMargin)
  }

}
