//Formatter
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

//auto rebuild
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// for Docker
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")

// ScalikeJDBC
// libraryDependencies += "com.h2database" % "h2" % "1.4.197"
// addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "3.3.2")
