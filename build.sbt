import java.net.InetAddress

import BuildUtils.env
import Dependencies._
import sbt.Keys._
import sbt.Package.ManifestAttributes

val libVersion = env("TRAVIS_TAG") orElse env("BUILD_LABEL") getOrElse s"1.0.0-${System.currentTimeMillis / 1000}-SNAPSHOT"

lazy val steve = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "steve"
  ).aggregate(steveCore, steveServer, steveScalaClient)

lazy val steveCore = (project in file("steve-core"))
  .settings(
    name := "steve-core",
    libraryDependencies ++= coreDependencies
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(sonatypePublishSettings: _*)
  .settings(publishForScalaTwoTen: _*)

lazy val steveServer = (project in file("steve-server"))
  .settings(
    name := "steve-server",
    libraryDependencies ++= serverDependencies
  )
  .settings(commonSettings: _*)
  .settings(steveAssembly: _*)
  .dependsOn(steveCore)
  .enablePlugins(JavaAppPackaging)

lazy val steveScalaClient = (project in file("steve-client-scala"))
  .settings(
    name := "steve-client-scala",
    libraryDependencies ++= clientDependencies
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(sonatypePublishSettings: _*)
  .settings(publishForScalaTwoTen: _*)
  .dependsOn(steveCore)

lazy val publishForScalaTwoTen = Seq(crossScalaVersions ++= Seq("2.10.4"))

lazy val commonSettings = Seq(
  version := libVersion,
  organization := "com.indix",
  packageOptions := Seq(ManifestAttributes(("Built-By", InetAddress.getLocalHost.getHostName))),
  parallelExecution in This := false,
  scalaVersion := "2.12.3",
  crossScalaVersions ++= Seq("2.12.3", "2.11.11"),
  organizationName := "Indix",
  organizationHomepage := Some(url("http://www.indix.com")),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7"),
  resolvers += Resolver.mavenLocal,
  resolvers += Resolver.sonatypeRepo("releases"),
  test in Test := {
    dumpLicenseReport.value
    (test in Test).value
  }
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishArtifact in(Compile, packageDoc) := true,
  publishArtifact in(Compile, packageSrc) := true,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra :=
    <url>https://github.com/ind9/steve</url>
      <licenses>
        <license>
          <name>Apache License</name>
          <url>https://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ind9/steve.git</url>
        <connection>scm:git:git@github.com:ind9/steve.git</connection>
      </scm>
      <developers>
        <developer>
          <id>indix</id>
          <name>Indix</name>
          <url>http://www.indix.com</url>
        </developer>
      </developers>
)

lazy val sonatypePublishSettings = Seq(
  /* START - sonatype publish related settings */
  useGpg := false,
  pgpSecretRing := file("local.secring.gpg"),
  pgpPublicRing := file("local.pubring.gpg"),
  pgpPassphrase := Some(sys.env.getOrElse("GPG_PASSPHRASE", "").toCharArray),
  credentials += Credentials("Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    System.getenv("SONATYPE_USERNAME"),
    System.getenv("SONATYPE_PASSWORD"))
  /* END - sonatype publish related settings */
)

lazy val steveAssembly = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList(ps@_*) if List("package-info.class", "plugin.properties", "mime.types").exists(ps.last.endsWith) =>
      MergeStrategy.first
    case "reference.conf" | "rootdoc.txt" =>
      MergeStrategy.concat
    case "LICENSE" | "LICENSE.txt" =>
      MergeStrategy.discard
    case PathList("META-INF", xs@_*) =>
      xs map {
        _.toLowerCase
      } match {
        case ("manifest.mf" :: Nil) =>
          MergeStrategy.discard
        case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
          MergeStrategy.discard
        case ("log4j.properties" :: Nil) =>
          MergeStrategy.discard
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.deduplicate
  }
)
