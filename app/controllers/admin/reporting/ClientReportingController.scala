package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Named, Singleton}
import org.joda.time.DateTime
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.Request
import services.SecurityService
import services.reporting._
import system.Roles

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
    DateTime.now().withTimeAtStartOfDay().minusDays(1),
    DateTime.now().withTimeAtStartOfDay()
  )

  private def defaultForm = form.fill(defaultData)
  
  private def render(data: DatedReportFormData, form: Form[DatedReportFormData] = defaultForm)(implicit req: Request[_]) = {
    Ok(views.html.admin.reporting.client.index(data.interval.getStartMillis, data.interval.getEndMillis, clientReportingService.getCacheLifetime, form))
  }

  def index = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    render(defaultData, defaultForm)
  }
  
  def formSubmit = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    defaultForm.bindFromRequest.fold(
      formError => render(defaultData, formError),
      data => render(data, defaultForm.fill(data))
    )
  }
  
  def report(start: String, end: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request => 
    val interval = clientReportingService.getInterval(start, end).getOrElse(defaultData.interval)
    clientReportingService.getMetrics(interval) match {
      case metrics if metrics.isComplete => Ok(views.html.admin.reporting.client.report(DateTime.now, Option(metrics)))
      case metrics if metrics.isFault => Ok(views.html.admin.reporting.client.report(DateTime.now, None))
      case _ => Accepted("Calculating...")
    }
  }
}
