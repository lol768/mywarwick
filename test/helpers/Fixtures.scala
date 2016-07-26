package helpers

import models.news.NewsItemSave
import models.{Activity, ActivitySave}
import org.joda.time.DateTime
import anorm._
import warwick.sso._

/**
  * Prebuilt or buildable fake data models for use in tests.
  */
object Fixtures {

  val user = UserFixtures

  object activitySave {

    lazy val submissionDue =
      ActivitySave(
        providerId = "tabula",
        `type` = "due",
        title = "Coursework due",
        text = Some("Your coursework is due in 7 days"),
        url = Some("http://tabula.warwick.ac.uk"),
        shouldNotify = true
      )

  }

  object activity {

    def fromSave(id: String, activity: ActivitySave) = Activity(
      id = id,
      providerId = activity.providerId,
      `type` = activity.`type`,
      title = activity.title,
      text = activity.text,
      url = activity.url,
      replacedBy = None,
      generatedAt = activity.generatedAt.getOrElse(DateTime.now),
      createdAt = DateTime.now,
      shouldNotify = activity.shouldNotify
    )

  }

  object news {
    def save(
      title: String = "Some news",
      text: String = "The news"
    ) = NewsItemSave(
      title = title,
      text = text,
      link = None,
      publishDate = DateTime.now,
      imageId = None,
      publisherId = "publisher",
      usercode = Usercode("custard")
    )
  }

  object sql {
    def insertPublisherPermission(
      publisherId: String,
      usercode: String,
      role: String
    ) = SQL"INSERT INTO PUBLISHER_PERMISSION (PUBLISHER_ID, USERCODE, ROLE) VALUES ($publisherId, $usercode, $role)"

    def insertActivityType(
      name: String,
      displayName: String
    ) = SQL"INSERT INTO ACTIVITY_TYPE (NAME, DISPLAY_NAME) VALUES ($name, $displayName)"
  }

}
