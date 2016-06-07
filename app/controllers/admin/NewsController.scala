package controllers.admin

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.DateFormats
import models.news.{Audience, Link, NewsItemSave}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{NewsService, PublishCategoryService, SecurityService}
import system.{Roles, TimeZones, Validation}
import uk.ac.warwick.util.web.Uri

import scala.concurrent.Future

case class NewsItemData(
  title: String,
  text: String,
  linkText: Option[String],
  linkHref: Option[String],
  publishDate: LocalDateTime,
  imageId: Option[String]
) {
  def toSave = NewsItemSave(
    title = title,
    text = text,
    link = for {
      t <- linkText
      h <- linkHref
    } yield Link(t, Uri.parse(h)),
    // TODO test this gives expected results of TZ&DST
    publishDate = publishDate.toDateTime(TimeZones.LONDON),
    imageId = imageId
  )
}

@Singleton
class NewsController @Inject()(
  security: SecurityService,
  val messagesApi: MessagesApi,
  news: NewsService,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder,
  val publishCategoryService: PublishCategoryService
) extends BaseController with I18nSupport with Publishing[NewsItemData] {

  import Roles._
  import security._

  val publishNewsForm = publishForm(mapping(
    "title" -> nonEmptyText,
    "text" -> nonEmptyText,
    "linkText" -> optional(text),
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url),
    "publishDate" -> DateFormats.dateTimeLocalMapping,
    "imageId" -> optional(text)
  )(NewsItemData.apply)(NewsItemData.unapply))

  def list = RequiredActualUserRoleAction(Sysadmin) {
    val theNews = news.allNews(limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    Ok(views.html.admin.news.list(theNews, counts))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin).async {
    for {
      dopts <- departmentOptions
    } yield {
      Ok(views.html.admin.news.createForm(publishNewsForm, dopts, categoryOptions))
    }
  }

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    departmentOptions.flatMap { dopts =>
      val bound = publishNewsForm.bindFromRequest
      bound.fold(
        errorForm => Future.successful(Ok(views.html.admin.news.createForm(errorForm, dopts, categoryOptions))),
        data => audienceBinder.bindAudience(data).map {
          case Left(errors) =>
            val errorForm = addFormErrors(bound, errors)
            Ok(views.html.admin.news.createForm(errorForm, dopts, categoryOptions))
          case Right(audience) =>
            handleForm(data, audience)
        }
      )
    }
  }

  def handleForm(data: Publish[NewsItemData], audience: Audience) = {
    val newsItem = data.item.toSave
    news.save(newsItem, audience, data.categoryIds)
    Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News created")
  }

}
