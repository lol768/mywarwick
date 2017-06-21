package controllers.api

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import play.api.libs.json.Json
import play.api.mvc.Action
import services.ActivityService
import services.dao.ActivityDao
import uk.ac.warwick.userlookup.UserLookupInterface
import warwick.sso.Usercode

@Singleton
class NotificationsSnapshotController @Inject() (@NamedDatabase("default") db: Database, lookup: UserLookupInterface, activityService: ActivityService) extends BaseController {

  val WARWICK_SSO_COOKIE_NAME: String = "WarwickSSO"

  /**
    * We need to be able to support users who don't have a session,
    * so we use the old-style `WarwickSSO` cookie token here. This isn't
    * ideal, but the data returned is of trivial significance.
    */
  def unreads = Action { implicit request =>
    val cookie = request.cookies.get(WARWICK_SSO_COOKIE_NAME)
    val optionUser = cookie.map(_.value).map(lookup.getUserByToken)

    if (optionUser.isEmpty) {
      Forbidden("You must possess a valid WarwickSSO cookie to do this, sorry.")
    } else {
      val user = optionUser.get
      val userCode = user.getUserId
      db.withConnection { implicit c =>
        Ok(Json.obj(
          "unreads" -> activityService.countUnreadNotificationsForUsercode(Usercode(userCode))
        ))
      }
    }

  }
}

