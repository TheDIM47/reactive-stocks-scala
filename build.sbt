import sbt.Keys._
import sbt.Project.projectToRef
import NativePackagerKeys._

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := Settings.versions.scala).
  jsConfigure(_ enablePlugins ScalaJSPlay).
  jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val clients = Seq(client)

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

// use eliding to drop some debug code in the production build
lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

lazy val server = (project in file("server")).settings(
  name := "server",
  version := Settings.versions.version,
  scalaVersion := Settings.versions.scala,
  scalacOptions ++= Settings.scalacOptions,
  libraryDependencies ++= Settings.jvmDependencies.value,
//    commands += ReleaseCmd,
  // connect to the client project
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd),
  // compress CSS
  LessKeys.compress in Assets := true
)
.enablePlugins(PlayScala)
.disablePlugins(PlayLayoutPlugin) // use the standard directory layout instead of Play's custom
.aggregate(clients.map(projectToRef): _*)
.dependsOn(sharedJVM)

lazy val client = (project in file("client")).settings(
  name := "client",
  version := Settings.versions.version,
  scalaVersion := Settings.versions.scala,
  scalacOptions ++= Settings.scalacOptions,
  libraryDependencies ++= Settings.scalajsDependencies.value,
  // by default we do development build, no eliding
  elideOptions := Seq(),
  scalacOptions ++= elideOptions.value,
  jsDependencies ++= Settings.jsDependencies.value,
  // RuntimeDOM is needed for tests
  jsDependencies += RuntimeDOM % "test",
  // yes, we want to package JS dependencies
  skip in packageJSDependencies := false,
  // use Scala.js provided launcher code to start the client app
  persistLauncher := true,
  persistLauncher in Test := false,
  // must specify source maps location because we use pure CrossProject
  sourceMapsDirectories += sharedJS.base / "..",
  // use uTest framework for tests
  testFrameworks += new TestFramework("utest.runner.Framework")
)
.enablePlugins(ScalaJSPlugin, ScalaJSPlay)
.dependsOn(sharedJS)

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
