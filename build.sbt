import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

name := conf.getString("application.name")

version := conf.getString("application.version")

libraryDependencies ++= Seq(
  javaCore,
  cache,
  javaJdbc,
  javaEbean,
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-email" % "1.3.3"
//    "com.sun.mail" % "javax.mail" % "1.5.2"
)     

play.Project.playJavaSettings
