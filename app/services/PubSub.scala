package services

import akka.actor.ActorSystem
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.google.inject.{Inject, ImplementedBy}

/**
  * Subject to renaming, destruction, etc.
  *
  * Low level PubSub service that publishes messages for subscribed worker
  * nodes to pick up and handle.
  */
@ImplementedBy(classOf[AkkaPubSub])
trait PubSub {
  def publish(topic: String, message: Any): Unit
}

class AkkaPubSub @Inject() (akka: ActorSystem) extends PubSub {
  private val pubsub = DistributedPubSub(akka)
  private val mediator = pubsub.mediator

  override def publish(topic: String, message: Any) = mediator ! Publish(topic, message)
}
