import ReleaseTransformations._

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
  version := "0.2.17",
  organization := "com.eztier",
  name := "cassandra-udt-codec-helper-scala",
  scalaVersion := "2.12.4",
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

val datastaxDriver = "com.datastax.cassandra" %  "cassandra-driver-core" % "3.4.0" 
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
val jodaTime = "joda-time" % "joda-time" % "2.9.9"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.9"
val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % "2.5.9"
val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.9" % Test
val alpakkaCassandra = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "0.18"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      datastaxDriver,
      scalaTest,
      scalaReflect.value,
      logback,
      jodaTime,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestkit,
      alpakkaCassandra
    )
  )

// Publishing
sonatypeProfileName := "com.eztier"

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/once-ler/cassandra-udt-codec-helper-scala"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := {_ => false}

releaseCrossBuild := false

releasePublishArtifactsAction := PgpKeys.publishSigned.value

// publishTo := Some(Resolver.file("file", new File("/home/htao/tmp")))

publishTo := sonatypePublishTo.value

/*
publishTo := Some(
  if (isSnapshot.value)
    "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
*/

scmInfo := Some(
  ScmInfo(
    browseUrl = url("https://github.com/once-ler/cassandra-udt-codec-helper-scala"),
    connection = "scm:git@github.com:once-ler/cassandra-udt-codec-helper-scala.git"
  )
)

developers := List(
  Developer(
    id = "once-ler",
    name = "Henry Tao",
    email = "htao@eztier.com",
    url = url("https://github.com/once-ler")
  )
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  // releaseStepCommand("publishSigned"),
  // publishArtifacts,
  setNextVersion,
  commitNextVersion,
  // releaseStepCommand("sonatypeRelease"),
  pushChanges
)


