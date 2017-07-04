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

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")