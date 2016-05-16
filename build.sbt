name := "MGP"

version := "0.0.4"

organization := "net.liftweb"

scalaVersion := "2.11.7"

resolvers ++= Seq("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "bone" at "http://bone.twbbs.org.tw/ivy",
  Resolver.sonatypeRepo("public"),
  Resolver.bintrayRepo("scalaz", "releases"))

Seq(webSettings: _*)

unmanagedResourceDirectories in Test <+= (baseDirectory) {
  _ / "src/main/webapp"
}

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.6.3"
  Seq(

    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftmodules" %% "omniauth_2.6" % "0.17" % "compile",
    "com.dropbox.core" % "dropbox-core-sdk" % "1.7.6" % "compile",
    "mysql" % "mysql-connector-java" % "5.1.38",
    "org.slf4j" % "slf4j-api" % "1.7.5" % "compile",
    "org.slf4j" % "slf4j-simple" % "1.7.5" % "compile",
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "org.scalikejdbc" %% "scalikejdbc" % "2.3.3",
    "com.zaxxer" % "HikariCP" % "2.4.3" % "compile",
    "net.liftmodules" % "fobo_2.6_2.11" % "1.5.1",
    "net.liftmodules" % "lift-jquery-module_2.6_2.11" % "2.9" % "compile",
    "org.eclipse.jetty" % "jetty-webapp" % "8.1.17.v20150415" % "container,test",
    "org.eclipse.jetty" % "jetty-plus" % "8.1.17.v20150415" % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.specs2" %% "specs2-core" % "3.6.4" % "test",
    "com.timesprint" % "hasher_2.10" % "0.3.0",
    "net.liftmodules" %% "extras_2-6" % "0.4",
    "net.liftmodules" %% "validate_2.6" % "1.0"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")
