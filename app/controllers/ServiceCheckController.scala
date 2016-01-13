package controllers

import javax.inject.Inject

import models.MessageState.{Available, Success}
import models.{DateFormats, QueueStatus}
import org.joda.time.DateTime
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.mvc._
import services.{ClusterStateService, HealthCheckService, SecurityService}

import scala.concurrent.Future

class ServiceCheckController @Inject()(
  cluster: ClusterStateService,
  security: SecurityService,
  life: ApplicationLifecycle,
  healthCheckService: HealthCheckService
) extends Controller {

  import healthCheckService._

  var stopping = false
  life.addStopHook(() => {
    stopping = true
    Future.successful(Unit)
  })

  def gtg = Action {
    if (stopping)
      ServiceUnavailable("Shutting down")
    else
      Ok("\"OK\"")
  }

  def healthcheck = Action {
    import DateFormats.isoDateWrites
    import Json._
    val state = cluster.state
    val members = state.members
    val unreachable = state.unreachable
    val messagesWaiting = messagingQueueStatus.filter(_.state == Available).map(_.count).sum

    Ok(obj(
      "data" -> arr(
        obj(
          "name" -> "messaging-queue",
          "status" -> (
            if (messagesWaiting < 100) "okay"
            else if (messagesWaiting < 300) "warning"
            else "critical"
          ),
          "perfData" -> messagingQueueStatus.filterNot(_.state == Success).map {
            case QueueStatus(status, output, count) =>
              s"${output}_queue_${status.dbValue}=$count".toLowerCase
          },
          "message" -> s"$messagesWaiting messages in queue",
          "testedAt" -> healthCheckLastRunAt
        ),
        obj(
          "name" -> "cluster-reachable",
          "status" -> (
            if (unreachable.isEmpty) "okay"
            else if (unreachable.size <= 2) "warning"
            else "critical"
          ),
          "perfData" -> arr(
            s"cluster_unreachable=${unreachable.size};1;2",
            s"cluster_reachable=${members.size - unreachable.size}"
          ),
          "message" -> (
            if (unreachable.isEmpty) "All members are reachable"
            else s"${unreachable.size} members are unreachable"
          ),
          "testedAt" -> new DateTime
        ),
        obj(
          "name" -> "cluster-size",
          "status" -> (
            if (members.isEmpty) "critical"
            else if (members.size == 1) "warning"
            else "okay"
          ),
          "perfData" -> arr(
            s"cluster_size=${members.size};1;0"
          ),
          "message" -> (
            if (members.isEmpty) "This node has not joined a cluster"
            else if (members.size == 1) s"Only one member () visible in the cluster"
            else s"${members.size} members in cluster"
          ),
          "testedAt" -> new DateTime
        )
      )
    ))

  }

}
