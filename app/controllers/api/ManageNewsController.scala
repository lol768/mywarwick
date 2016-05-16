package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.Action
import services.dao.NewsDao
import services.{FeedService, NewsService}
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ManageNewsController @Inject()(
  newsService: NewsService,
  feedService: FeedService,
  newsDao: NewsDao
) extends BaseController {


  def create = Action {

    NotImplemented("Nothing")
  }

}
