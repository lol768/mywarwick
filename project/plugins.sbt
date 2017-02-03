// Warwick parent plugin
resolvers += "nexus" at "https://mvn.elab.warwick.ac.uk/nexus/content/groups/public"
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
addSbtPlugin("uk.ac.warwick" % "play-warwick" % "0.5")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.12")

// .tgz generator
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.3")

// Code coverage
addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.6")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.4.0")

// web plugins

//addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

//addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

//addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

//addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

//addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.5.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
