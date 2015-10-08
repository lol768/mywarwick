package actors

import javax.inject.Singleton

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification

final case class MessageEnvelope(topic: String, payload: Any)

/**
 * WIP. Thing for subscribing actors to certain kinds of events.
 *
 * Note that this MessageBus would only be within a single node, so
 * probably not that useful in its current form except for demonstration.
 * There are ways to make a clustered message bus, they just need a little
 * more work.
 *
 * topics are dot-separated classifiers, and you can subscribe to
 * a group of topics with a common prefix, e.g. topics "a.b.c"
 * and "a.b.d" can be subscribed to by subscribing to topic "a.b"
 */
@Singleton
class MessageBus extends EventBus with SubchannelClassification {
  type Event = MessageEnvelope
  type Classifier = String
  type Subscriber = ActorRef

  override protected def classify(event: MessageEnvelope): String = event.topic

  override protected implicit def subclassification: Subclassification[String] = new Subclassification[String] {
    override def isEqual(x: String, y: String): Boolean = (x == y)
    override def isSubclass(x: String, y: String): Boolean =
      x.startsWith(y) &&
      x.split("\\.").startsWith(y.split("\\."))
  }

  override protected def publish(event: MessageEnvelope, subscriber: ActorRef): Unit =
    subscriber ! event.payload
}
