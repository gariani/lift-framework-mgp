//Enable the sbt web plugin
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.7.0")

//Enable the sbt idea plugin
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.10")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

resolvers += "Typesafe Public Repo" at "http://repo.typesafe.com/typesafe/releases"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

libraryDependencies ++= Seq(
  "org.apache.velocity" % "velocity" % "1.7"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")
