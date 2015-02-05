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
  var context: Context = null
}