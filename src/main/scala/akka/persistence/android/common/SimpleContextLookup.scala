package akka.persistence.android.common

import android.content.Context

/**
 * a simple implementation of ContextLookup. It requires setContext to be called before starting the ActorSystem.
 * this is implemented as a class and companion object so we can instantiate the class by string without using Scala's
 * reflection, which is dependent on the java.rmi package which isn't available on Android
 */
class SimpleContextLookup extends ContextLookup {
  def getContext: Option[Context] = SimpleContextLookup.context

  def setContext(context: Context): Unit = {
    SimpleContextLookup.context match {
      case Some(c) => if (c != context) throw new IllegalArgumentException("context cannot be changed once set")
      case None => SimpleContextLookup.context = Some(context)
    }
  }
}

/**
 * companion class acts as singleton to hole the context we're passing around
 */
private object SimpleContextLookup {
  var context: Option[Context] = None
}
