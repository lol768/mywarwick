package controllers.admin.reporting

import controllers.MyController
import javax.inject.{Inject, Named, Singleton}
import org.joda.time.{DateTime, Interval}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.JodaForms.jodaDate
import play.api.i18n.I18nSupport
import play.api.mvc.Request
import services.SecurityService
import services.reporting.ActivityReportingService
import system.Roles

import scala.concurrent.ExecutionContext

@Singleton
class ActivityReportingController @Inject()(
  activityReportingService: ActivityReportingService,
  securityService: SecurityService,
)(implicit @Named("web") ec: ExecutionContext) extends MyController with I18nSupport {

  import Roles._
  import securityService._

  private val form = DatedReportFormData.form

  private def defaultData = DatedReportFormData(
    DateTime.now().minusDays(14),
    DateTime.now()
  )

  private def defaultForm = form.fill(defaultData)

  def index = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    render(defaultData, defaultForm)
  }

  def formSubmit = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    defaultForm.bindFromRequest.fold(
      formError => render(defaultData, formError),
      data => render(data, defaultForm.fill(data))
    )
  }

  private def render(data: DatedReportFormData, form: Form[DatedReportFormData])(implicit req: Request[_]) =
    activityReportingService.allAlertsCountByProviders(data.interval).map { result =>
      Ok(views.html.admin.reporting.activity.index(result, form))
    }
}

