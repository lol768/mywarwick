package services

import javax.inject.Inject

import akka.actor.Actor.Receive
import akka.actor.{Address, Actor, ActorSystem}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import com.google.inject.ImplementedBy
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@ImplementedBy(classOf[ClusterStateServiceImpl])
trait ClusterStateService {
  def selfAddress: Address
  def state: CurrentClusterState
}

object ClusterStateServiceImpl {
  class Updates extends Actor {
    override def receive: Receive = ???
  }
}

class ClusterStateServiceImpl @Inject() (
  akka: ActorSystem,
  life: ApplicationLifecycle
) extends ClusterStateService {
  val cluster = Cluster(akka)

  life.addStopHook(() => {
    Future.successful(cluster.leave(cluster.selfAddress))
  })

  override def state = cluster.state
  override def selfAddress = cluster.selfAddress
}
