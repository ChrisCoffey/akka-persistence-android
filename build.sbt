organization := "me.leaf"

name := "akka-persistence-android"

version := "0.1"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation"
)

javacOptions ++= Seq(
  "-source","1.7",
  "-target","1.7"
)

parallelExecution in Test := false

val akkaVersion = "2.3.8"
val mauricioVersion = "0.2.15"

resolvers += "RoboTest releases" at "https://raw.github.com/zbsz/mvn-repo/master/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka"     %% "akka-actor"                        % akkaVersion,
  "com.typesafe.akka"     %% "akka-persistence-experimental"     % akkaVersion,
  "org.scaloid"           %% "scaloid"                           % "3.6.1-10",
  "org.robolectric" % "android-all" % "5.0.0_r2-robolectric-0" % "provided",  // android version used by Robolectric 2.4
  "com.android.support" % "support-v4" % "19.0.1",
  "com.geteit" %% "robotest" % "0.7" % "test",                              // latest RoboTest version
  "junit" % "junit" % "4.8.2" % "test",                                     // needed to run tests
  "com.typesafe.akka"     %% "akka-persistence-tck-experimental" % akkaVersion     % "test",
  "com.typesafe.akka"   %% "akka-slf4j"                        % akkaVersion     % "test",
  "com.typesafe.akka"     %% "akka-testkit"                      % akkaVersion     % "test",
  "org.slf4j"            % "slf4j-log4j12"                     % "1.7.10"        % "test"
)

fork in Test := true

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>https://github.com/okumin/akka-persistence-sql-async</url>
  <licenses>
    <license>
      <name>Apache 2 License</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:okumin/akka-persistence-sql-async.git</url>
    <connection>scm:git:git@github.com:okumin/akka-persistence-sql-async.git</connection>
  </scm>
  <developers>
    <developer>
      <id>okumin</id>
      <name>okumin</name>
      <url>http://okumin.com/</url>
    </developer>
  </developers>
)
