package controllers.admin

import com.google.inject.Inject
import controllers.BaseController
import play.api.mvc.Call
import services.{PublisherService, SecurityService}

class PublishersController @Inject()(
  securityService: SecurityService,
  publisherService: PublisherService
) extends BaseController {

  import securityService._

  def notifications = handlePublisherRedirect(routes.NotificationsController.list)

  def news = handlePublisherRedirect(routes.NewsController.list)

  def handlePublisherRedirect(route: (String) => Call) = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    publisherService.getPublishersForUser(user.usercode) match {
      case Nil =>
        Forbidden(views.html.errors.forbidden(user.name.first))
      case Seq(publisher) =>
        Redirect(route(publisher.id))
      case publishers =>
        Ok(views.html.admin.publishers(publishers, route))
    }
  }

}
