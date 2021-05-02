lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.13.3"
    )),
    name := "test-canal"
  )

lazy val akkaVersion = "2.6.14"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "2.0.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.typesafe" % "config" % "1.4.1",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "org.xerial" % "sqlite-jdbc" % "3.28.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "2.0.2",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)

mainClass in (Compile, run) := Some("com.canal.Main")
mainClass in (Compile, packageBin) := Some("com.canal.Main")

enablePlugins(JavaAppPackaging)