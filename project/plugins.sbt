// Warwick parent plugin
resolvers += "nexus" at "https://mvn.elab.warwick.ac.uk/nexus/content/groups/public"
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
addSbtPlugin("uk.ac.warwick" % "play-warwick" % "0.1-SNAPSHOT")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.3")


// web plugins

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

resolvers += "nexus" at "https://mvn.elab.warwick.ac.uk/nexus/content/groups/public"

//addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

//addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

//addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

//addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.5.2")
