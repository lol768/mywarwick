package controllers.publish

import com.google.inject.Inject
import controllers.BaseController
import models.publishing.Publisher
import services.{PublisherService, SecurityService}
import warwick.sso.{User, UserLookupService}

class PublishersController @Inject()(
  securityService: SecurityService,
  publisherService: PublisherService,
  userLookup: UserLookupService
) extends BaseController {

  import securityService._

  def index = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    val publishers = publisherService.getPublishersForUser(user.usercode)

    val publisherPermissions: Map[Publisher, Seq[User]] =
      publishers.map(publisher => publisher -> publisherService.getPublisherPermissions(publisher.id)).toMap
        .mapValues(_.flatMap(permission =>
          userLookup.getUser(permission.usercode).toOption
        ).sortBy(u => (u.name.last.getOrElse(""), u.name.first.getOrElse(""))))


    if (publishers.isEmpty) {
      Forbidden(views.html.publish.publishers.index(Nil, Map.empty))
    } else if (publishers.size == 1) {
      Redirect(routes.PublishersController.show(publishers.head.id))
    } else {
      Ok(views.html.publish.publishers.index(publishers, publisherPermissions))
    }
  }

  def show(publisherId: String) = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    val publishers = publisherService.getPublishersForUser(user.usercode)
    val role = publisherService.getRoleForUser(publisherId, user.usercode)

    publishers.find(_.id == publisherId)
      .map(publisher => Ok(views.html.publish.publishers.show(publisher, role)))
      .getOrElse(Forbidden(views.html.errors.forbidden(user.name.first)))
  }

}
