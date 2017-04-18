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
        "groups" -> Json.arr()
      ))
    } else {
      groupService.getGroupsForQuery(query.trim) match {
        case Success(groups) =>
          val results = (scope match {
            case AllDepartments => groups
            case Departments(departments) => groups.filter(d => departments.contains(d.department.code.get))
          }).take(8)

          Ok(Json.obj(
            "groups" -> Json.toJson(results)
          ))
        case Failure(e) =>
          logger.warn(s"Error fetching WebGroups matching query '${query.trim}'", e)
          InternalServerError(Json.obj(
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
