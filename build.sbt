name := "akka-typed-playground"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit-typed" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)
