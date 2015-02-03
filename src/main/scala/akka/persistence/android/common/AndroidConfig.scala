package akka.persistence.android.common

import akka.actor.ActorSystem

private[persistence] class AndroidConfig(val system: ActorSystem) {
  val config = system.settings.config.getConfig(AndroidConfig.rootKey)
  val name = config.getString(AndroidConfig.nameKey)
  val contextLookupClass = config.getString(AndroidConfig.contextLookupKey)
}

private[persistence] object AndroidConfig {
  val rootKey = "akka-persistence-android"
  val nameKey = "name"
  val contextLookupKey = "context-lookup.class"

  def apply(system: ActorSystem): AndroidConfig = new AndroidConfig(system)
}
