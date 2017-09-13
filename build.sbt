import java.net.InetAddress

import Dependencies._
import sbt.Keys._
import sbt.Package.ManifestAttributes

lazy val steve = Project(
  id = "steve",
  base = file("."),
  //  settings = defaultSettings,
  aggregate = Seq(steveCore, steveServer, steveScalaClient)
)

lazy val steveCore = (project in file("steve-core"))
  .settings(
    name := "steve-core",
    libraryDependencies ++= coreDependencies
  )
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(sonatypePublishSettings: _*)

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
  .dependsOn(steveCore)

lazy val commonSettings = Seq(
  organization := "com.indix",
  packageOptions := Seq(ManifestAttributes(("Built-By", InetAddress.getLocalHost.getHostName))),
  parallelExecution in This := false,
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3"),
  organizationName := "Indix",
  organizationHomepage := Some(url("http://www.indix.com")),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7"),
  resolvers += "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += "releases"  at "https://oss.sonatype.org/content/repositories/releases"
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
        <url>git@github.com:indix/steve.git</url>
        <connection>scm:git:git@github.com:indix/steve.git</connection>
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
  useGpg := true,
  pgpPassphrase := Some(Array())
  /* END - sonatype publish related settings */
)

lazy val steveAssembly = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if List("package-info.class", "plugin.properties", "mime.types").exists(ps.last.endsWith) =>
      MergeStrategy.first
    case "reference.conf" | "rootdoc.txt" =>
      MergeStrategy.concat
    case "LICENSE" | "LICENSE.txt" =>
      MergeStrategy.discard
    case PathList("META-INF", xs @ _*) =>
      xs map {
        _.toLowerCase
      } match {
        case ("manifest.mf" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
          MergeStrategy.discard
        case ("log4j.properties" :: Nil) =>
          MergeStrategy.discard
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.deduplicate
  }
)
