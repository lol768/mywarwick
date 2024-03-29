package helpers

import anorm._
import models.news.{NewsItemRender, NewsItemSave}
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import warwick.sso._

/**
  * Prebuilt or buildable fake data models for use in tests.
  */
object Fixtures {

  val user = UserFixtures

  object activitySave {

    lazy val submissionDue =
      ActivitySave(
        changedBy = Usercode("custard"),
        publisherId = "elab",
        providerId = "tabula",
        `type` = "due",
        title = "Coursework due",
        text = Some("Your coursework is due in 7 days"),
        url = Some("http://tabula.warwick.ac.uk"),
        shouldNotify = true
      )

    lazy val submissionDueFromApi =
      ActivitySave(
        changedBy = Usercode("custard"),
        publisherId = "elab",
        providerId = "tabula",
        `type` = "due",
        title = "Coursework due",
        text = Some("Your coursework is due in 7 days"),
        url = Some("http://tabula.warwick.ac.uk"),
        shouldNotify = true,
        api = true
      )

    lazy val submissionDueWithoutUrl =
      ActivitySave(
        changedBy = Usercode("custard"),
        publisherId = "elab",
        providerId = "tabula",
        `type` = "due",
        title = "Coursework due",
        text = Some("Your coursework is due in 7 days"),
        url = None,
        shouldNotify = true
      )

    lazy val activityFromApi =
      ActivitySave(
        changedBy = Usercode("custard"),
        publisherId = "elab",
        providerId = "tabula",
        `type` = "due",
        title = "Coursework due",
        text = Some("Your coursework is due in 7 days"),
        url = Some("http://tabula.warwick.ac.uk"),
        shouldNotify = true,
        api = true
      )

  }

  object activity {

    val aTime = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime("12/02/2018 11:11:11")

    def fromSave(id: String, activity: ActivitySave) = Activity(
      id = id,
      providerId = activity.providerId,
      `type` = activity.`type`,
      title = activity.title,
      text = activity.text,
      url = activity.url,
      replacedBy = None,
      publishedAt = activity.publishedAt.getOrElse(aTime),
      createdAt = aTime,
      createdBy = activity.changedBy,
      shouldNotify = activity.shouldNotify,
      api = activity.api
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

    val render = NewsItemRender(
      id = "news",
      title = "Some news",
      text = "The news",
      link = None,
      publishDate = DateTime.now,
      imageId = None,
      categories = Seq.empty,
      ignoreCategories = false,
      publisherId = "publisher"
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
