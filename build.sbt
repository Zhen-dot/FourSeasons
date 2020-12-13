name := "FourSeasons"

version := "0.1"

scalaVersion := "2.12.10"

val AkkaVersion = "2.6.10"
val AkkaManagementVersion = "1.0.9"

resolvers += ("custome1" at "http://4thline.org/m2").withAllowInsecureProtocol(true)
//  "Local Maven Repository" at "file:///" + Path.userHome + "/.ivy2/cache"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))

libraryDependencies ++= Seq(
  "org.apache.derby" % "derby" % "10.13.1.1",
  "com.h2database" % "h2" % "1.4.196",
  "org.scalikejdbc" %% "scalikejdbc" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.fourthline.cling" % "cling-core" % "2.1.2",
  "org.fourthline.cling" % "cling-support" % "2.1.2",
  "org.scalafx" % "scalafx_2.12" % "8.0.144-R12",
  "org.scalafx" % "scalafxml-core-sfx8_2.12" % "0.4",
  "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion
)

fork := true
