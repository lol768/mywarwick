package controllers.publish

import com.google.inject.Inject
import controllers.BaseController
import models.publishing.{Publisher, PublisherPermission}
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

    val publisherPermissions: Map[Publisher, Seq[PublisherPermission]] =
      publishers.map(publisher => publisher -> publisherService.getPublisherPermissions(publisher.id)).toMap

    val userMap = userLookup.getUsers(publisherPermissions.mapValues(_.map(_.usercode)).values.flatten.toSeq)
      .getOrElse(Map.empty)

    val publisherUserPermissions: Map[Publisher, Seq[User]] =
      publishers.map(publisher => publisher -> publisherService.getPublisherPermissions(publisher.id)).toMap
        .mapValues(_.flatMap(permission => userMap.get(permission.usercode))
          .sortBy(u => (u.name.last.getOrElse(""), u.name.first.getOrElse("")))
          .distinct
        )

    if (publishers.isEmpty) {
      Forbidden(views.html.publish.publishers.index(Nil, Nil, Map.empty))
    } else {
      val (globalPublishers, normalPublishers) = publisherService.partitionGlobalPublishers(publishers)
      Ok(views.html.publish.publishers.index(globalPublishers, normalPublishers, publisherUserPermissions))
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
