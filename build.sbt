lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Ylog-classpath"
)

lazy val commonSettings = Seq(
  version := "0.2.13",
  organization := "com.eztier",
  scalaVersion := "2.12.4",
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

val datastaxDriver = "com.datastax.cassandra" %  "cassandra-driver-core" % "3.4.0" 
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
val jodaTime = "joda-time" % "joda-time" % "2.9.9"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.9"
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.5.9"
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.9" % Test
val alpakkaCassandra = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.18"
val hapiV231 = "ca.uhn.hapi" % "hapi-structures-v231" % "2.3"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "test-scalaxb",
    libraryDependencies ++= Seq(
      datastaxDriver,
      scalaTest,
      logback,
      jodaTime,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestkit,
      alpakkaCassandra,
      hapiV231
    )
  )