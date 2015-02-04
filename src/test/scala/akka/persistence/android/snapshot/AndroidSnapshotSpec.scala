package akka.persistence.android.snapshot

import akka.persistence.android.AppContext
import akka.persistence.android.common.SimpleContextLookup
import akka.persistence.snapshot.SnapshotStoreSpec
import com.typesafe.config.ConfigFactory
import org.robolectric.annotation.Config
import org.scalatest.RobolectricSuite

@Config(manifest = "src/test/AndroidManifest.xml")
class AndroidSnapshotSpec extends SnapshotStoreSpec with RobolectricSuite {
  (new SimpleContextLookup).setContext(AppContext.context)
  lazy val config = ConfigFactory.load("application.conf")
}
