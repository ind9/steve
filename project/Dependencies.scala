import sbt._

object Dependencies {
  val dropwizard = "com.datasift.dropwizard.scala" %% "dropwizard-scala-core" % "1.1.0-2"

  val logbackVersion = "1.1.7"
  val logbackCore    = "ch.qos.logback" % "logback-core" % logbackVersion
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val logging        = Seq(logbackCore, logbackClassic)

  val scalaTest = "org.scalatest" %% "scalatest"   % "3.0.4"   % Test
  val mockito   = "org.mockito"   % "mockito-core" % "1.10.19" % Test

  val testDependencies = Seq(scalaTest, mockito)

  lazy val coreDependencies = testDependencies
  lazy val serverDependencies = Seq(dropwizard) ++ testDependencies

}
