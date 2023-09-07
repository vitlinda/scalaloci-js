import Dependencies._
import org.scalajs.sbtplugin.ScalaJSPlugin

ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.11"

ThisBuild / resolvers += "jitpack" at "https://jitpack.io"

ThisBuild / scalaJSUseMainModuleInitializer := true


lazy val scalaLociVersion = "130f6d7259"
lazy val scafiVersion = "1.1.6"
lazy val scalaTestVersion = "3.2.16"

val macroparadise = scalacOptions += "-Ymacro-annotations"


def standardDirectoryLayout(directory: File): Seq[Def.Setting[_]] =
  standardDirectoryLayout(Def.setting { directory })

def standardDirectoryLayout(directory: Def.Initialize[File]): Seq[Def.Setting[_]] = Seq(
  Compile / unmanagedSourceDirectories += directory.value / "src" / "main" / "scala",
  Compile / unmanagedResourceDirectories += directory.value / "src" / "main" / "resources",
  Test / unmanagedSourceDirectories += directory.value / "src" / "test" / "scala",
  Test / unmanagedResourceDirectories += directory.value / "src" / "test" / "resources")

val sharedDirectories =
  standardDirectoryLayout(Def.setting { baseDirectory.value.getParentFile / "shared" })

val sharedMultitierDirectories =
  standardDirectoryLayout(Def.setting { baseDirectory.value.getParentFile })

val settingsMultitier =
  sharedMultitierDirectories ++ Seq(macroparadise,
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-Xlint", "-Ymacro-annotations", "-Ywarn-unused"),
    libraryDependencies ++= Seq(
      "io.github.scala-loci.scala-loci" %%% "scala-loci-language" % scalaLociVersion % "provided",
      "io.github.scala-loci.scala-loci" %%% "scala-loci-language-runtime" % scalaLociVersion,
      "io.github.scala-loci.scala-loci" %%% "scala-loci-language-transmitter-rescala" % scalaLociVersion,
      "io.github.scala-loci.scala-loci" %%% "scala-loci-communicator-tcp" % scalaLociVersion,
      "io.github.scala-loci.scala-loci" %%% "scala-loci-serializer-upickle" % scalaLociVersion,
      "io.github.scala-loci.scala-loci" %%% "scala-loci-serializer-circe" % scalaLociVersion,
      "io.github.scala-loci.scala-loci" %%% "scala-loci-communicator-ws-webnative" % scalaLociVersion,
      "io.github.scala-loci.scala-loci" %%% "scala-loci-communicator-ws-jetty" % scalaLociVersion))

lazy val chat = (project in file(".")
  aggregate (chatMultiObserve))

lazy val chatMultiObserve = (project in file(".") / ".all"
  settings (Compile / run :=
  ((chatMultiObserveJVM / Compile / run) dependsOn
    (chatMultiObserveJS / Compile / fastLinkJS)).evaluated)
  aggregate (chatMultiObserveJVM, chatMultiObserveJS))

lazy val chatMultiObserveJVM = (project in file(".") / ".jvm"
  settings (settingsMultitier: _*)
  settings (
  Compile / mainClass := Some("example.Pinger"),
  Compile / resources ++=
    ((chatMultiObserveJS / Compile / crossTarget).value ** "*.js").get))

lazy val chatMultiObserveJS = (project in file(".") / ".js"
  settings (settingsMultitier: _*)
  settings (
  Compile / mainClass := Some("example.Ponger"),
  Compile / scalaJSUseMainModuleInitializer := true)
  enablePlugins ScalaJSPlugin)
