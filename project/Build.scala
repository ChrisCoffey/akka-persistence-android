import sbt._
import sbt.Keys._

object Build extends android.AutoBuild {

  lazy val akka_persistence_android = Project (
    "akka-persistence-android",
    file("."),
    settings = buildSettings ++ releasePluginSettings ++ Seq(
      resolvers := repos,
      libraryDependencies ++= dependencies
    )
  )

  import ReleaseRepositories._

  val buildSettings = Seq(
    organization := "me.leaf",
    scalaVersion := "2.11.4",
    scalacOptions ++= Seq(
      "-unchecked",
      "-feature",
      "-deprecation"
    ),
    javacOptions ++= Seq(
      "-source","1.7",
      "-target","1.7"
    ),
    parallelExecution in Test := false,
    fork in Test := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishTo := Some(if (isSnapshot.value) leafSnapshots else leafReleases)
  )

  val repos = Seq(
    "RoboTest releases" at "https://raw.github.com/zbsz/mvn-repo/master/releases/"
  )

  val akkaVersion = "2.3.8"

  val dependencies = Seq(
    "com.typesafe.akka"     %% "akka-actor"                         % akkaVersion,
    "com.typesafe.akka"     %% "akka-persistence-experimental"      % akkaVersion,
    "org.scaloid"           %% "scaloid"                            % "3.6.1-10",
    "org.robolectric"        % "android-all"                        % "5.0.0_r2-robolectric-0"    % "provided", // android version used by Robolectric 2.4
    "com.android.support"    % "support-v4"                         % "19.0.1",
    "com.geteit"            %% "robotest"                           % "0.7"                       % "test",     // latest RoboTest version
    "junit"                  % "junit"                              % "4.8.2"                     % "test",     // needed to run Roboelectric
    "com.typesafe.akka"     %% "akka-persistence-tck-experimental"  % akkaVersion                 % "test",
    "com.typesafe.akka"     %% "akka-slf4j"                         % akkaVersion                 % "test",
    "com.typesafe.akka"     %% "akka-testkit"                       % akkaVersion                 % "test",
    "org.slf4j"              % "slf4j-log4j12"                      % "1.7.10"                    % "test"
  )

  // sbt release plugin configuration
  val releasePluginSettings = {
    import sbtrelease._
    import ReleasePlugin._
    import ReleaseKeys._

    releaseSettings ++ Seq(
      versionBump := Version.Bump.Minor,
      versionFile := file("project/version.sbt")
    )
  }
}