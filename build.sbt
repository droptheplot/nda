ThisBuild / scalaVersion := "3.1.2"
ThisBuild / version := "0.1.0-SNAPSHOT"

val http4sVersion = "1.0.0-M30"

lazy val root = (project in file("."))
  .settings(
    name := "nda",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.0-RC6",
      "dev.zio" %% "zio-test" % "2.0.0-RC6" % Test,
      "dev.zio" %% "zio-interop-cats" % "3.3.0-RC7",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
