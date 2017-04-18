package controllers.publish

import javax.inject.Inject

import controllers.BaseController
import models.publishing.PermissionScope.{AllDepartments, Departments}
import play.api.libs.json._
import play.api.mvc.Action
import services.PublisherService
import warwick.sso.{Group, GroupService}

import scala.util.{Failure, Success}

class WebGroupsController @Inject()(
  groupService: GroupService,
  publisherService: PublisherService
) extends BaseController {

  def results(publisherId: String, query: String) = Action {
    val scope = publisherService.getPermissionScope(publisherId)

    if (query.trim.isEmpty) {
      BadRequest(Json.obj(
        "success" -> false,
        "status" -> "bad_request",
        "groups" -> Json.arr()
      ))
    } else {
      groupService.getGroupsForQuery(query.trim) match {
        case Success(groups) =>
          val results = (scope match {
            case AllDepartments => groups
            case Departments(departments) => groups.filter(_.department.code.exists(departments.contains))
          }).take(8)

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
