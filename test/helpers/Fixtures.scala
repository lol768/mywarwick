package helpers

import models.news.NewsItemSave
import models.{Activity, ActivitySave}
import org.joda.time.DateTime
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

  def mockLoginContext(u: Option[User]) = new LoginContext {
    override val user: Option[User] = u
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"

    override def userHasRole(role: RoleName) = false

    override def actualUserHasRole(role: RoleName) = false
  }

}
