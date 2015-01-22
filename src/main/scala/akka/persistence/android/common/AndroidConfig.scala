package akka.persistence.android.common

import akka.actor.ActorSystem

private[persistence] class AndroidConfig(val system: ActorSystem) {
  val rootKey = "akka-persistence-android"
  val config = system.settings.config.getConfig(rootKey)

  val name = config.getString("name")
}

private[persistence] object AndroidConfig {
  def apply(system: ActorSystem): AndroidConfig = new AndroidConfig(system)
}