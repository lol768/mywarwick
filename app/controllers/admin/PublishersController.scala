package controllers.admin

import com.google.inject.Inject
import controllers.BaseController
import services.{PublisherService, SecurityService}

class PublishersController @Inject()(
  securityService: SecurityService,
  publisherService: PublisherService
) extends BaseController {

  import securityService._

  def index = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    val publishers = publisherService.getPublishersForUser(user.usercode)

    if (publishers.isEmpty) {
      Forbidden(views.html.admin.publishers.index(Nil))
    } else if (publishers.size == 1) {
      Redirect(routes.PublishersController.show(publishers.head.id))
    } else {
      Ok(views.html.admin.publishers.index(publishers))
    }
  }

  def show(publisherId: String) = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    val publishers = publisherService.getPublishersForUser(user.usercode)
    val role = publisherService.getRoleForUser(publisherId, user.usercode)

    publishers.find(_.id == publisherId)
      .map(publisher => Ok(views.html.admin.publishers.show(publisher, role)))
      .getOrElse(Forbidden(views.html.errors.forbidden(user.name.first)))
  }

}
