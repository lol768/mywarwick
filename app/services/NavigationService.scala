package services

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import controllers.admin.{routes => adminRoutes}
import controllers.admin.publishers.{routes => adminPublishersRoutes}
import controllers.publish.{routes => publishRoutes}
import models.publishing.Ability.{ViewNews, ViewNotifications}
import models.publishing.{Ability, Publisher}
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
  publisherService: PublisherService,
  featuresService: FeaturesService
) extends NavigationService {

  def getNavigation(loginContext: LoginContext): Seq[Navigation] = loginContext.user.map { user =>
    val publishers = publisherService.getPublishersForUser(user.usercode)

    Seq(
      publishers.nonEmpty ->
        NavigationPage("Publishing",
          publishRoutes.PublishersController.index(),
          publishers.map(navigationForPublisher(_, user))
        ),

      loginContext.userHasRole(Sysadmin) ->
        sysadminNavigation
    ).collect { case (condition, page) if condition => page }
  }.getOrElse(Seq.empty)

  def navigationForPublisher(publisher: Publisher, user: User): Navigation = {
    val publishingRole = publisherService.getRoleForUser(publisher.id, user.usercode)
    val features = featuresService.get(Option(user))

    val children: Seq[NavigationPage] = Seq(
        if (features.news) ViewNews -> NavigationPage("News", publishRoutes.NewsController.list(publisher.id)) else null,
        ViewNotifications -> NavigationPage("Alerts", publishRoutes.NotificationsController.list(publisher.id))
      )
      .filterNot { _ == null }
      .collect {
        case (ability, page) if publishingRole.can(ability) => page
      }

    NavigationPage(publisher.name, publishRoutes.PublishersController.show(publisher.id), children)
  }

  val sysadminNavigation: Navigation = {
    NavigationDropdown("Sysadmin", adminRoutes.AdminController.sysadmin(), Seq(
      NavigationPage("Publishers", adminPublishersRoutes.PublishersController.index()),
      NavigationPage("Masquerade", adminRoutes.MasqueradeController.masquerade()),
      NavigationPage("Cluster State", adminRoutes.ClusterStateController.html()),
      NavigationPage("ElasticSearch", controllers.admin.elasticsearch.routes.ActivityToESController.index()),
      NavigationPage("EAP Features", adminRoutes.EAPFeaturesController.index()),
      NavigationPage("Reports", controllers.admin.reporting.routes.HomeController.index(), Seq(
        NavigationPage("Preferences Report", controllers.admin.reporting.routes.PreferencesReportingController.index()),
        NavigationPage("Activity Report", controllers.admin.reporting.routes.ActivityReportingController.index())
      ))
    ))
  }
}
