package services.healthcheck

import com.google.inject.Inject
import org.joda.time.DateTime
import services.ClusterStateService

class ClusterSizeHealthCheck @Inject()(
  cluster: ClusterStateService
) extends HealthCheck[Int] {

  override def name = "cluster-size"

  override def value = cluster.state.members.size

  override def warn = 1

  override def critical = 0

  override def message = value match {
    case 0 => "This node has not joined a cluster"
    case 1 => "Only one member visible in the cluster"
    case n => s"$n members in cluster"
  }

  override def testedAt = DateTime.now

}
