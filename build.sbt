ThisBuild / scalaVersion := "3.1.2"
ThisBuild / version := "0.1.0-SNAPSHOT"

val http4sVersion = "1.0.0-M30"
val zioVersion = "2.0.0-RC6"
val doobieVersion = "1.0.0-RC2"

lazy val root = (project in file("."))
  .settings(
    name := "nda",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-interop-cats" % "3.3.0-RC7",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .enablePlugins(SbtTwirl)
