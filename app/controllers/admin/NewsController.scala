package controllers.admin

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.DateFormats
import models.news.{Audience, Link, NewsItemRender, NewsItemSave}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{AudienceService, NewsService, SecurityService}
import system.{Roles, TimeZones}
import uk.ac.warwick.util.web.Uri

import scala.concurrent.Future

case class PublishNewsData(
  item: NewsItemData,
  audience: Seq[String],
  department: Option[String]
) extends AudienceFormData

case class NewsItemUpdate(
  id: String,
  item: NewsItemData
)

case class NewsItemData(
  title: String,
  text: String,
  linkText: Option[String],
  linkHref: Option[String],
  publishDate: LocalDateTime
) {
  def toSave = NewsItemSave(
    title = title,
    text = text,
    link = for {
      t <- linkText
      h <- linkHref
    } yield Link(t, Uri.parse(h)),
    // TODO test this gives expected results of TZ&DST
    publishDate = publishDate.toDateTime(TimeZones.LONDON)
  )
}

@Singleton
class NewsController @Inject()(
  security: SecurityService,
  val messagesApi: MessagesApi,
  news: NewsService,
  departments: DepartmentInfoDao,
  audiences: AudienceService,
  audienceBinder: AudienceBinder
) extends BaseController with I18nSupport {

  import Roles._
  import security._
  import system.ThreadPools.web

  val departmentTypes = Set("ACADEMIC", "SERVICE")
  val departmentInitialValue = Seq("" -> "--- Department ---")

  def departmentOptions = departments.allDepartments
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
    "publishDate" -> DateFormats.dateTimeLocalMapping
  )(NewsItemData.apply)(NewsItemData.unapply)

  type PublishNewsForm = Form[PublishNewsData]

  val publishNewsForm = Form(
    mapping(
      "item" -> newsItemMapping,
      "audience" -> seq(nonEmptyText),
      "department" -> optional(text)
    )(PublishNewsData.apply)(PublishNewsData.unapply)
  )

  //  type NewsForm = Form[NewsItemData]

  val newsUpdateForm = Form(
    mapping(
      "id" -> nonEmptyText,
      "item" -> newsItemMapping
    )(NewsItemUpdate.apply)(NewsItemUpdate.unapply)
  )

  def list = RequiredActualUserRoleAction(Sysadmin) {
    val theNews = news.allNews(limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.admin.news.list(newsPending, newsPublished, counts))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin).async {
    for {
      dopts <- departmentOptions
    } yield {
      Ok(views.html.admin.news.createForm(publishNewsForm, dopts))
    }
  }

  def addErrors[A](form: Form[A], errors: Seq[FormError]) = errors.foldLeft(form)(_.withError(_))

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    departmentOptions.flatMap { dopts =>
      val bound = publishNewsForm.bindFromRequest
      bound.fold(
        errorForm => Future.successful(Ok(views.html.admin.news.createForm(errorForm, dopts))),
        data => audienceBinder.bindAudience(data).map {
          case Left(errors) =>
            val errorForm = addErrors(bound, errors)
            Ok(views.html.admin.news.createForm(errorForm, dopts))
          case Right(audience) =>
            handleForm(data, audience)
        }
      )
    }
  }

  def handleForm(data: PublishNewsData, audience: Audience) = {
    val newsItem = data.item.toSave
    news.save(newsItem, audience)
    Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News created")
  }

  def handleUpdate(id: String, data: NewsItemData) = {
    news.updateNewsItem(id, data)
    Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News updated")
  }

  def update(id: String) = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    departmentOptions.flatMap { dopts =>
      val bound = Form(newsItemMapping).bindFromRequest
      bound.fold(
        errorForm => Future.successful(Ok(views.html.admin.news.updateForm(id, errorForm, dopts))),
        data => Future(handleUpdate(id, data))
      )
    }
  }

  def unpublish(id: String) = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    news.unpublish(id)
    Ok()
  }

  def updateForm(id: String) = RequiredActualUserRoleAction(Sysadmin).async {
    val item = news.get(id)
    item match {
      case None => Future(NotFound(s"Cannot update news. No news item exists with id '$id'"))
      case Some(news) =>
        for {
          dopts <- departmentOptions
        } yield {
          Ok(views.html.admin.news.updateForm(id, Form(newsItemMapping).fill(news.toData), dopts))
        }
    }
  }

  def partitionNews(news: Seq[NewsItemRender]) = news.partition(_.publishDate.isAfterNow)
}
