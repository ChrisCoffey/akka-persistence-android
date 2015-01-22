import sbt._

object ReleaseRepositories {

  private val leafRepository = "http://repo.leafcorp.me/"
  val leafSnapshots = "snapshots" at leafRepository + "content/repositories/snapshots"
  val leafReleases = "releases"  at leafRepository + "content/repositories/releases"
}