package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.DateFormats
import models.news.Audience.{DepartmentAudience, DepartmentSubset}
import models.news.{Audience, Link, NewsItemSave}
import org.joda.time.{DateTime, LocalDate, LocalDateTime}
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{AudienceService, NewsService, SecurityService}
import system.{Roles, TimeZones}
import uk.ac.warwick.util.core.StringUtils
import uk.ac.warwick.util.web.Uri
import warwick.sso.Usercode

import scala.concurrent.Future

case class PublishNewsData(
  item: NewsItemData,
  audience: Seq[String],
  department: Option[String]
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

class NewsController @Inject() (
  security: SecurityService,
  val messagesApi: MessagesApi,
  news: NewsService,
  departments: DepartmentInfoDao,
  audiences: AudienceService
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

  /**
    * Attempts to convert the request parameters into an Audience object.
    * If there are any problems it returns a Seq of FormErrors.
    *
    * Return type is a future because it depends on the list of departments.
    *
    * TODO move this out of the controller, for use with notifications and the API.
    */
  def parseAudience(data: PublishNewsData): Future[Either[Seq[FormError], Audience]] = for {
    allDepts <- departments.allDepartments
  } yield {
    var errors = Seq[FormError]()

    val groupedComponents = data.audience.groupBy(_.startsWith("Dept:"))

    // Bits of audience not related to a department.
    val globalComponents = groupedComponents.getOrElse(false, Nil).flatMap {
      case Audience.ComponentParameter(component) => Some(component)
      case unrecognised =>
        errors :+= FormError("audience", "error.audience.invalid", unrecognised)
        None
    }

    val deptComponentValues = groupedComponents.getOrElse(true, Nil)
      .map(_.replaceFirst("^Dept:",""))
      .flatMap {
        case Audience.DepartmentSubset(subset) => Some(subset)
        case unrecognised =>
          errors :+= FormError("audience", "error.audience.invalid", s"Dept:$unrecognised")
          None
      }

    val departmentParam = data.department.filter(StringUtils.hasText).map(_.trim)
    val department = departmentParam.flatMap { code =>
      allDepts.find(_.code == code)
    }
    val deptComponent = department match {
      case Some(d) if deptComponentValues.nonEmpty => Some(DepartmentAudience(d.code, deptComponentValues))
      case Some(d) =>
        errors :+= FormError("department", "error.audience.noDepartmentSubsets")
        None
      case None if data.department.isDefined =>
        errors :+= FormError("department", "error.department.invalid")
        None
      case None => None // No department audience - no problem
    }

    if (department.isEmpty && globalComponents.isEmpty) {
      errors :+= FormError("audience", "error.audience.empty")
    }

    if (errors.nonEmpty) {
      Left(errors)
    } else {
      Right(Audience(globalComponents ++ deptComponent))
    }
  }

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

  def addErrors[A](form: Form[A], errors: Seq[FormError]) = errors.foldLeft(form)(_.withError(_))

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    departmentOptions.flatMap { dopts =>
      val bound = publishNewsForm.bindFromRequest
      bound.fold(
        errorForm => Future.successful(Ok(views.html.admin.news.createForm(errorForm, dopts))),
        data => parseAudience(data).map {
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

}
