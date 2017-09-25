import org.apache.commons.lang3.StringUtils
import sbt._

object Dependencies {
  val dropwizard = "com.datasift.dropwizard.scala" %% "dropwizard-scala-core" % "1.1.0-2"
  val guice = "com.hubspot.dropwizard" % "dropwizard-guice" % "1.0.6.0"
  val postgres = "org.postgresql" % "postgresql" % "42.1.4"
  val slick = "com.typesafe.slick" %% "slick" % "3.2.1"
  val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"
  val slickPg = "com.github.tminglei" %% "slick-pg" % "0.15.3"
  val typesafe = "com.typesafe" % "config" % "1.3.1"
  val flyway = "org.flywaydb" % "flyway-core" % "4.2.0"
  val scalajHttp = "org.scalaj" %% "scalaj-http" % "2.3.0"
  val logbackVersion = "1.1.7"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVersion
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val logging = Seq(logbackCore, logbackClassic)

  val fasterxmlScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.7"


  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
  val mockito = "org.mockito" % "mockito-core" % "1.10.19" % Test

  val testDependencies = Seq(scalaTest, mockito)

  lazy val coreDependencies = Seq(fasterxmlScala) ++ testDependencies
  lazy val serverDependencies = Seq(dropwizard, guice, postgres, slick, slickHikaricp, slickPg, typesafe, flyway) ++ testDependencies
  lazy val clientDependencies = Seq(scalajHttp, fasterxmlScala) ++ testDependencies
}

object BuildUtils {
  def env(key: String) = sys.env.get(key).filter(StringUtils.isNotBlank)
}