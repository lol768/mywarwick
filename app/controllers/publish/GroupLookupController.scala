package controllers.publish

import com.google.inject.{Inject, Singleton}
import controllers.BaseController
import models.API
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Result
import services.dao.{LookupModule, LookupRelationshipType, LookupSeminarGroup}
import services.{GroupLookupService, SecurityService}
import warwick.sso._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class GroupLookupQuery(query: String)


@Singleton
class GroupLookupController @Inject()(
  securityService: SecurityService,
  lookupService: GroupLookupService
) extends BaseController {

  import securityService._

  implicit val lookupModuleWrites: Writes[LookupModule] = Json.writes[LookupModule]
  implicit val lookupSeminarGroupWrites: Writes[LookupSeminarGroup] = Json.writes[LookupSeminarGroup]
  implicit val lookupRelationshipTypeWrites: Writes[LookupRelationshipType] = Json.writes[LookupRelationshipType]

  private val form = Form[GroupLookupQuery](
    mapping(
      "query" -> nonEmptyText
    )(GroupLookupQuery.apply)(GroupLookupQuery.unapply)
  )

  // TODO: option to limit module searches to within specified departments
  def queryModule = RequiredUserAction.async { implicit request =>
    form.bindFromRequest.fold[Future[Result]](
      hasErrors => Future(Ok(Json.toJson(API.Error("invalid", s"Json was invalid when querying modules. ${request.body.asJson.get}")))),
      success => {
        val query = success.query.trim
        lookupService.findModule(query).map { foundModules =>
          Ok(Json.toJson(Map("modules" -> foundModules)))
        }
      }
    )
  }

  // TODO: option to limit seminar group searches to within specified departments
  def querySeminarGroup = RequiredUserAction.async { implicit request =>
    form.bindFromRequest.fold[Future[Result]](
      hasErrors => Future(Ok(Json.toJson(API.Error("invalid", s"Json was invalid when querying seminar groups. ${request.body.asJson.get}")))),
      success => {
        val query = success.query.trim
        lookupService.findSeminarGroup(query).map { foundSeminarGroups =>
          Ok(Json.toJson(Map("seminarGroups" -> foundSeminarGroups)))
        }
      }
    )
  }

  def queryRelationships = RequiredUserAction.async { implicit request =>
    form.bindFromRequest.fold[Future[Result]](
      hasErrors => Future(Ok(Json.toJson(API.Error("invalid", s"Json was invalid when querying staff relationships. ${request.body.asJson.get}")))),
      success => {
        val query = success.query.trim
        val agentId = UniversityID(query)
        lookupService.findRelationships(agentId).map { foundRelationships =>
          Ok(Json.obj(
            "relationships" -> foundRelationships.map {
              case (r, u) => Json.obj(
                r.id -> Json.obj(
                  "agentRole" -> r.agentRole,
                  "studentRole" -> r.studentRole,
                  "students" -> u.map(_.name.full)
                )
              )
            }
          ))
        }
      }
    )
  }

}
