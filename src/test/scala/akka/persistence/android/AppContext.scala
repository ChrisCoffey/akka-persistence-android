package akka.persistence.android

import android.app.Application
import android.content.Context

class AppContext extends Application {
  override def onCreate() {
    super.onCreate()
    AppContext.context = getApplicationContext
  }
}

object AppContext {
  // scalastyle:off null - this is hack to get the context for tests
  var context: Context = null
  // scalastyle:on null
}
