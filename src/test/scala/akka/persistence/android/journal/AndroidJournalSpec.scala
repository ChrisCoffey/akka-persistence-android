package akka.persistence.android.journal

import akka.persistence.journal.JournalSpec
import com.typesafe.config.ConfigFactory
import org.robolectric.annotation.Config
import org.scalatest.RobolectricSuite

@Config(manifest = "src/main/AndroidManifest.xml")
class AndroidJournalSpec extends JournalSpec with RobolectricSuite {
  lazy val config = ConfigFactory.load("application.conf")
}