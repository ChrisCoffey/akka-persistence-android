package akka.persistence.android.snapshot

import akka.persistence.snapshot.SnapshotStoreSpec
import com.typesafe.config.ConfigFactory
import org.robolectric.annotation.Config
import org.scalatest.RobolectricSuite

@Config(manifest = "src/main/AndroidManifest.xml")
class AndroidSnapshotSpec extends SnapshotStoreSpec with RobolectricSuite {
  lazy val config = ConfigFactory.load("application.conf")
}
