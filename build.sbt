import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

name := conf.getString("application.name")

version := conf.getString("application.version")

libraryDependencies ++= Seq(
    javaCore,
    cache,
    javaJdbc,
    javaEbean,
    "commons-io" % "commons-io" % "2.4"
)     

play.Project.playJavaSettings
