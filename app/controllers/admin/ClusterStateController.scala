package controllers.admin

import javax.inject.{Singleton, Inject}

import akka.cluster.Member
import controllers.MyController
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Action
import services.{ClusterStateService, SecurityService}
import system.Roles
import system.Roles.Sysadmin

/**
  * Experimental.
  *
  * Lists the cluster members as understood by this node. When
  * nodes are coming and going it's possible for each node to
  * have a slightly different member list, but when things are
  * stable they ought to be the same.
  *
  * If any members are "unreachable", then no new members can
  * be brought into
  */
@Singleton
class ClusterStateController @Inject() (
  cluster: ClusterStateService,
  security: SecurityService
) extends MyController {

  import security._

  def html = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.clusterstate(cluster.state, cluster.selfAddress))
  }

  def get = Action {
    val state = cluster.state
    Ok(Json.obj(
      "leader" -> JsString(state.leader.map(_.toString).orNull),
      "unreachable" -> render(state.unreachable),
      "members" -> render(state.members)
    ))
  }

  private def render(members: Set[Member]) = members.map { member =>
    member.address.toString -> member.status.getClass.getSimpleName.replace("$","")
  }.toMap[String, String]

}
