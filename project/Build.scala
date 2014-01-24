import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "DSS_Server"
  val appVersion      = "0.51.3-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    cache,
    javaJdbc,
    javaEbean,
    "commons-io" % "commons-io" % "2.4"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

  playAssetsDirectories <+= baseDirectory / "tempFiles"
}
