package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.DateFormats
import models.news.{Link, NewsItemSave}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Action
import services.SecurityService
import services.dao.NewsDao
import system.{Roles, TimeZones}
import uk.ac.warwick.util.web.Uri
import warwick.sso.Usercode

case class PublishNewsData(
  item: NewsItemData,
  recipients: String
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
  db: Database,
  dao: NewsDao // FIXME direct dao! I'll write a service in a minute, promise
) extends BaseController with I18nSupport {

  import security._
  import Roles._

  val jodaDateTime = of(Formats.jodaDateTimeFormat("yyyy-MM-dd'T'HH:mm", TimeZones.LONDON))

  val newsItemMapping = mapping(
    "title" -> nonEmptyText,
    "text" -> nonEmptyText,
    "linkText" -> optional(text),
    "linkHref" -> optional(text),
    "publishDate" -> jodaDateTime
  )(NewsItemData.apply)(NewsItemData.unapply)

  type PublishNewsForm = Form[PublishNewsData]

  val publishNewsForm = Form(
    mapping(
      "item" -> newsItemMapping,
      "recipients" -> nonEmptyText
    )(PublishNewsData.apply)(PublishNewsData.unapply)
  )

  def list = RequiredActualUserRoleAction(Sysadmin) {
    db.withConnection { implicit c =>
      Ok(views.html.admin.news.list(dao.allNews(limit = 100)))
    }
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin) {
    Ok(views.html.admin.news.createForm(publishNewsForm))
  }

  def create = RequiredActualUserRoleAction(Sysadmin) { implicit req =>
    publishNewsForm.bindFromRequest.fold(
      errorForm => Ok(views.html.admin.news.createForm(errorForm)),
      data => db.withConnection { implicit c =>
        val recipients = data.recipients.split(",").map(_.trim).map(Usercode)
        val newsItem = data.item.toSave
        dao.save(newsItem, recipients)
        Redirect(controllers.admin.routes.NewsController.list())
      }
    )
  }

}
