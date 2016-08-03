package services

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import controllers.admin.{routes => adminRoutes}
import models.publishing.Ability.{ViewNews, ViewNotifications}
import models.publishing.Publisher
import play.api.mvc.Call
import system.Roles.Sysadmin
import warwick.sso.{LoginContext, RoleService, User}

sealed trait Navigation {
  def label: String

  def route: Call

  def children: Seq[Navigation]

  def dropdown: Boolean

  /**
    * Either this is the current page, or the current page is a child of this page
    */
  def isActive(path: String): Boolean = path.startsWith(route.url) || children.exists(_.isActive(path))

  def deepestActive(path: String): Option[Navigation] =
    if (path.startsWith(route.url) && !children.exists(_.isActive(path))) Some(this)
    else children.flatMap(_.deepestActive(path)).headOption
}

case class NavigationPage(
  label: String,
  route: Call,
  children: Seq[Navigation] = Nil
) extends Navigation {
  val dropdown = false
}

case class NavigationDropdown(
  label: String,
  route: Call,
  children: Seq[Navigation]
) extends Navigation {
  val dropdown = true
}

@ImplementedBy(classOf[NavigationServiceImpl])
trait NavigationService {
  def getNavigation(loginContext: LoginContext): Seq[Navigation]
}

@Singleton
class NavigationServiceImpl @Inject()(
  roleService: RoleService,
  publisherService: PublisherService
) extends NavigationService {

  def getNavigation(loginContext: LoginContext): Seq[Navigation] = loginContext.user.map { user =>
    val publishers = publisherService.getPublishersForUser(user.usercode)

    Seq(
      publishers.nonEmpty ->
        NavigationPage("Publishing",
          adminRoutes.PublishersController.index(),
          publishers.map(navigationForPublisher(_, user))
        ),

      loginContext.userHasRole(Sysadmin) ->
        sysadminNavigation
    ).collect { case (condition, page) if condition => page }
  }.getOrElse(Seq.empty)

  def navigationForPublisher(publisher: Publisher, user: User): Navigation = {
    val publishingRole = publisherService.getRoleForUser(publisher.id, user.usercode)

    val children = Seq(
      ViewNews -> NavigationPage("News", adminRoutes.NewsController.list(publisher.id)),
      ViewNotifications -> NavigationPage("Notifications", adminRoutes.NotificationsController.list(publisher.id))
    ).collect { case (ability, page) if publishingRole.can(ability) => page }

    NavigationPage(publisher.name, adminRoutes.PublishersController.show(publisher.id), children)
  }

  val sysadminNavigation: Navigation = {
    NavigationDropdown("Sysadmin", adminRoutes.AdminController.sysadmin(), Seq(
      NavigationPage("Masquerade", adminRoutes.MasqueradeController.masquerade()),
      NavigationPage("Cluster State", adminRoutes.ClusterStateController.html())
    ))
  }
}
