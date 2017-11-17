package controllers.admin.reporting

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import play.api.i18n.{I18nSupport, MessagesApi}
import services.SecurityService
import services.reporting.ActivityReportingService
import system.Roles

import scala.collection.immutable.ListMap

@Singleton
class ActivityReportingController @Inject()(
  activityReportingService: ActivityReportingService,
  securityService: SecurityService,
  val messagesApi: MessagesApi
) extends BaseController with I18nSupport {

  import Roles._
  import securityService._

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.reporting.activity.index())
  }
}
