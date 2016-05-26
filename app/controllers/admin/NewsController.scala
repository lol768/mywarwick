package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.DateFormats
import models.news.{Link, NewsItemSave}
import org.joda.time.DateTime
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{NewsService, SecurityService}
import system.{Roles, TimeZones}
import uk.ac.warwick.util.web.Uri
import warwick.sso.Usercode

case class PublishNewsData(
  item: NewsItemData,
  recipients: Option[String],
  audience: Seq[String]
)

case class NewsItemData(
  title: String,
  text: String,
  linkText: Option[String],
  linkHref: Option[String],
  publishDate: DateTime = new DateTime()
) {
  def toSave = NewsItemSave(
    title = title,
    text = text,
    link = for {
        t <- linkText
        h <- linkHref
      } yield Link(t, Uri.parse(h)),
    publishDate = publishDate
  )
}

class NewsController @Inject() (
  security: SecurityService,
  val messagesApi: MessagesApi,
  news: NewsService,
  departments: DepartmentInfoDao
) extends BaseController with I18nSupport {

  import system.ThreadPools.web
  import Roles._
  import security._

  val departmentTypes = Set("ACADEMIC", "SERVICE")
  val departmentInitialValue = Seq("" -> "--- Department ---")
  lazy val departmentOptions = departments.allDepartments
    .recover { case e => Nil }
    .map { depts =>
      departmentInitialValue ++ depts.filter { info => departmentTypes.contains(info.`type`) }
        .sortBy { info => info.name }
        .map { info => info.code -> info.name }
    }

  val newsItemMapping = mapping(
    "title" -> nonEmptyText,
    "text" -> nonEmptyText,
    "linkText" -> optional(text),
    "linkHref" -> optional(text),
    "publishDate" -> DateFormats.jodaDateTimeMapping
  )(NewsItemData.apply)(NewsItemData.unapply)

  type PublishNewsForm = Form[PublishNewsData]

  val publishNewsForm = Form(
    mapping(
      "item" -> newsItemMapping,
      "recipients" -> optional(nonEmptyText),
      "audience" -> seq(nonEmptyText)
    )(PublishNewsData.apply)(PublishNewsData.unapply)
  )

  def list = RequiredActualUserRoleAction(Sysadmin) {
    Ok(views.html.admin.news.list(news.allNews(limit = 100)))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin).async {
    for {
      dopts <- departmentOptions
    } yield {
      Ok(views.html.admin.news.createForm(publishNewsForm, dopts))
    }
  }

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    for {
      dopts <- departmentOptions
    } yield {
      publishNewsForm.bindFromRequest.fold(
        errorForm => Ok(views.html.admin.news.createForm(errorForm, dopts)),
        data => {
          val recipients = data.recipients.getOrElse("").split(",").map(_.trim).map(Usercode)
          val newsItem = data.item.toSave
          news.save(newsItem, recipients)
          Redirect(controllers.admin.routes.NewsController.list())
        }
      )
    }
  }

}
