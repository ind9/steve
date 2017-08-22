import java.net.InetAddress

import Dependencies._
import sbt.Package.ManifestAttributes

lazy val steve = Project(
  id = "steve",
  base = file("."),
  //  settings = defaultSettings,
  aggregate = Seq(steveCore)
)

lazy val steveCore = (project in file("steve-core"))
  .settings(
  name := "steve-core",
  libraryDependencies ++= coreDependencies
  ).settings(publishSettings: _*)
  .settings(sonatypePublishSettings: _*)

lazy val commonSettings = Seq(
  organization := "com.indix",
  packageOptions := Seq(ManifestAttributes(("Built-By", InetAddress.getLocalHost.getHostName))),
  parallelExecution in This := false,
  scalaVersion := "2.10.4",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3"),
  organizationName := "Indix",
  organizationHomepage := Some(url("http://www.indix.com")),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7")
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishArtifact in (Compile, packageDoc) := true,
  publishArtifact in (Compile, packageSrc) := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
)

lazy val sonatypePublishSettings = Seq(
  /* START - sonatype publish related settings */
  useGpg := true,
  pgpPassphrase := Some(Array())
  /* END - sonatype publish related settings */
)
