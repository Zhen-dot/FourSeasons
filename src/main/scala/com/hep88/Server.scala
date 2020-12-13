package com.hep88

import com.hep88.util.Database
import com.hep88.protocol.FSConfig
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.cluster.typed.{Cluster, Join}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

object Server extends App {
  val config = ConfigFactory.load()
  val mainSystem = ActorSystem("MainSystem", FSConfig.askDevConfig().withFallback(config))
  val cluster = Cluster(mainSystem.toTyped)

  //  Database.clear()
  println("Starting server...")
  Database.setup()

  cluster.manager ! Join(cluster.selfMember.address)
  AkkaManagement(mainSystem).start()
  ClusterBootstrap(mainSystem).start()
  mainSystem.spawn(ServerActor(), "ServerActor")

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() {
    def run() {
      Database.shutdown()
    }
  }))
}
