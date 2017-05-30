package controllers.publish

import javax.inject.Inject

import controllers.BaseController
import models.publishing.PermissionScope.{AllDepartments, Departments}
import play.api.libs.json._
import services.{PublisherService, SecurityService}
import warwick.sso.{Group, GroupService}

import scala.util.{Failure, Success}

class WebGroupsController @Inject()(
  groupService: GroupService,
  val publisherService: PublisherService,
  val securityService: SecurityService
) extends BaseController with PublishingActionRefiner {

  val excludedGroupSuffixes = Seq(
    "-all",
    "-staff",
    "-studenttype-undergraduate-full-time",
    "-studenttype-undergraduate-part-time",
    "-studenttype-postgraduate-full-time",
    "-studenttype-postgraduate-part-time",
    "-studenttype-postgraduate-taught-ft",
    "-studenttype-postgraduate-taught-pt",
    "-teaching"
  )

  def results(publisherId: String, rawQuery: String) = PublisherAction(publisherId) {
    val query = rawQuery.trim
    val scope = publisherService.getPermissionScope(publisherId)

    if (query.isEmpty) {
      BadRequest(Json.obj(
        "success" -> false,
        "status" -> "bad_request",
        "groups" -> Json.arr()
      ))
    } else {
      groupService.getGroupsForQuery(query) match {
        case Success(groups) =>
          val allMatches = scope match {
            case AllDepartments => groups
            case Departments(departments) => groups.filter(_.department.code.flatMap(Option.apply).map(_.toUpperCase).exists(departments.map(_.toUpperCase).contains))
          }

          val results = allMatches
            .map(group => (group, group.name.string.indexOf(query)))
            .filter { case (_, index) => index >= 0 }
            .sortBy { case (_, index) => index }
            .map { case (group, _) => group }
            .filterNot(group => excludedGroupSuffixes.exists(group.name.string.endsWith))
            .take(8)

          Ok(Json.obj(
            "success" -> true,
            "status" -> "ok",
            "groups" -> Json.toJson(results)
          ))
        case Failure(e) =>
          logger.warn(s"Error fetching WebGroups matching query '${query.trim}'", e)
          InternalServerError(Json.obj(
            "success" -> false,
            "status" -> "internal_server_error",
            "groups" -> Json.arr()
          ))
      }
    }
  }

  implicit val writesGroup = new Writes[Group] {
    override def writes(o: Group): JsValue = Json.obj(
      "name" -> o.name.string,
      "title" -> o.title
    )
  }

}
