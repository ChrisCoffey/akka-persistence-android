package akka.persistence.android.journal

import akka.persistence.android.AppContext
import akka.persistence.android.common.SimpleContextLookup
import akka.persistence.journal.JournalSpec
import com.typesafe.config.ConfigFactory
import org.robolectric.annotation.Config
import org.scalatest.RobolectricSuite

@Config(manifest = "src/test/AndroidManifest.xml")
class AndroidJournalSpec extends JournalSpec with RobolectricSuite {
  (new SimpleContextLookup).setContext(AppContext.context)
  lazy val config = ConfigFactory.load("application.conf")
}