package akka.persistence.android.common

import android.content.Context

trait ContextLookup {
  def getContext: Option[Context]
}
