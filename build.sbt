name := "akka-typed-playground"

version := "1.0"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)
