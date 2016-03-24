package services.healthcheck

import com.google.inject.Inject
import org.joda.time.DateTime
import services.ClusterStateService

class ClusterUnreachableHealthCheck @Inject()(
  cluster: ClusterStateService
) extends HealthCheck[Int] {

  override def name = "cluster-unreachable"

  override def value = cluster.state.unreachable.size

  override def warning = 1

  override def critical = 2

  override def message = value match {
    case 0 => "No members are unreachable"
    case n => s"$n members are unreachable"
  }

  override def testedAt = DateTime.now

}
