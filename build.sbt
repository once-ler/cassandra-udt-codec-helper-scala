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
  version := "0.1.1",
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

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "test-scalaxb",
    libraryDependencies ++= Seq(
      datastaxDriver,
      scalaTest,
      logback,
      jodaTime
    )
  )