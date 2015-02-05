import com.typesafe.sbt.pgp.PgpKeys
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

object AkkaPersistenceAndroidBuild extends Build {

  lazy val akka_persistence_android = Project (
    "akka-persistence-android",
    file("."),
    settings = buildSettings
      ++ scalariformSettings
      ++ releasePluginSettings
      ++ Seq(
        resolvers := repos,
        libraryDependencies ++= dependencies
      )
  )

  val buildSettings = Seq(
    organization := "me.leaf",
    scalaVersion := "2.11.5",
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
    credentials += Credentials(Path.userHome / ".ivy2" / ".nexus-credentials"),
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(DoubleIndentClassDeclaration, false)
      .setPreference(PreserveDanglingCloseParenthesis, false),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/leafme/akka-persistence-android")),
    pomExtra :=
      <scm>
        <url>git@github.com:leafme/akka-persistence-android.git</url>
        <developerConnection>scm:git:git@github.com:leafme/akka-persistence-android.git</developerConnection>
        <connection>scm:git:git@github.com:leafme/akka-persistence-android.git</connection>
      </scm>
      <developers>
        <developer>
          <name>Mike Roberts</name>
          <email>mroberts@leaf.me</email>
          <organization>Leaf</organization>
          <organizationUrl>http://www.leaf.me</organizationUrl>
        </developer>
      </developers>
  )

  val repos = Seq(
    "RoboTest Releases" at "https://raw.github.com/zbsz/mvn-repo/master/releases/",
    "Leaf 3rd Party" at "http://repo.leafcorp.me/content/repositories/thirdparty/"
  )

  val akkaVersion = "2.3.8"

  val dependencies = Seq(
    "com.typesafe.akka"     %% "akka-actor"                         % akkaVersion,
    "com.typesafe.akka"     %% "akka-persistence-experimental"      % akkaVersion,
    "org.scaloid"           %% "scaloid"                            % "3.6.1-10",
    "org.robolectric"        % "android-all"                        % "5.0.0_r2-robolectric-0"    % "provided", // android version used by Robolectric 2.4
    "com.android.support"    % "support-v4"                         % "19.0.1"                    % "provided",
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
      versionFile := file("project/version.sbt"),
      publishArtifactsAction := PgpKeys.publishSigned.value,
      useGlobalVersion := false
    )
  }
}
