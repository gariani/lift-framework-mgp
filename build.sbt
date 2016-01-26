name := "MGP"

version := "0.0.4"

organization := "net.liftweb"

scalaVersion := "2.11.6"

resolvers ++= Seq("snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "https://oss.sonatype.org/content/repositories/releases")

Seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.6.2"
  Seq(
    "mysql"                    % "mysql-connector-java"   % "5.1.38",
    "org.slf4j"                % "slf4j-api"              % "1.7.5"            % "compile",
    "org.slf4j"                % "slf4j-simple"           % "1.7.5"            % "compile",
    "net.liftweb"             %% "lift-webkit"            % liftVersion        % "compile",
    "org.scalikejdbc"         %% "scalikejdbc"            % "2.3.3",
    "com.zaxxer"               % "HikariCP"               % "2.4.3"            % "compile",
    "org.webjars.bower"        %  "angularjs"             % "1.4.8",
    "net.liftmodules"         %% ("ng_2.6")               % "0.9.0"               % "compile",
    "net.liftweb"             %% "lift-webkit"            % "2.6.2"               % "compile",
    "org.eclipse.jetty"        % "jetty-webapp"           % "8.1.17.v20150415"    % "container,test",
    "org.eclipse.jetty"        % "jetty-plus"             % "8.1.17.v20150415"    % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit"  % "javax.servlet"          % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"           % "logback-classic"        % "1.1.3",
    "org.specs2"              %% "specs2-core"            % "3.6.4"               % "test",
    "net.liftmodules"         %% "fobo_2.6"               % "1.5"                 % "compile",
    "com.timesprint"           % "hasher_2.10"            % "0.3.0",
    "net.liftmodules"         %% "fobo_2.6"               % "1.4"                 % "compile"
  )
}

scalacOptions in Test ++= Seq("-Yrangepos")
