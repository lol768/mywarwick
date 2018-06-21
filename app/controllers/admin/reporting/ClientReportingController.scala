package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Named, Singleton}
import org.joda.time.DateTime
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.Request
import services.SecurityService
import services.reporting.ClientReportingService
import system.Roles
import warwick.sso.Department

import scala.concurrent.ExecutionContext

@Singleton
class ClientReportingController @Inject()(
  clientReportingService: ClientReportingService,
  securityService: SecurityService,
)(implicit @Named("web") ec: ExecutionContext) extends MyController with I18nSupport {

  import Roles._
  import securityService._

  private val form = DatedReportFormData.form

  private def defaultData = DatedReportFormData(
    DateTime.now().minusDays(7),
    DateTime.now()
  )

  private def defaultForm = form.fill(defaultData)

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    render(defaultData, defaultForm)
  }

  def formSubmit = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    defaultForm.bindFromRequest.fold(
      formError => render(defaultData, formError),
      data => render(data, defaultForm.fill(data))
    )
  }

  private def render(data: DatedReportFormData, form: Form[DatedReportFormData])(implicit req: Request[_]) = {
    val metrics = ClientMetrics(
      clientReportingService.available,
      clientReportingService.countUniqueUsers(data.interval),
      clientReportingService.countAppUsers(data.interval),
      clientReportingService.countWebUsers(data.interval),
      clientReportingService.countUniqueUsersByDepartment(data.interval),
      clientReportingService.countUniqueUsersByType(data.interval)
    )
    
    Ok(views.html.admin.reporting.client.index(DateTime.now, metrics, form))
  }
}

case class ClientMetrics(
  available: Boolean = false,
  uniqueUserCount: Int = 0,
  appUserCount: Int = 0,
  webUserCount: Int =0 ,
  deptUserCount: Map[Option[Department], Int] = Map.empty,
  typedUserCount: Map[String, Int] = Map.empty
)

object ClientMetrics {
  def empty = ClientMetrics()
}
