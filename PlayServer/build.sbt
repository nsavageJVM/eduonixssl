name := "eduonixssl"

version := "1.0"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)

oneJarSettings

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.apache.mina" % "mina-core" % "2.0.4"

libraryDependencies += "org.apache.sshd" % "sshd-core" % "0.14.0"

libraryDependencies += "commons-codec" % "commons-codec" % "1.10"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

play.Project.playJavaSettings

mainClass in (Compile, run) := Some("EntryPoint")
