package helpers

import models.{Activity, ActivityRecipients, ActivityPrototype}

import org.joda.time.DateTime

/**
  * Prebuilt or buildable fake data models for use in tests.
  */
object Fixtures {

  val user = UserFixtures

  object activityPrototype {

    lazy val submissionDue =
      ActivityPrototype(
        providerId = "tabula",
        `type` = "due",
        title = "Coursework due",
        text = Some("Your coursework is due in 7 days"),
        url = Some("http://tabula.warwick.ac.uk"),
        tags = Seq.empty,
        replace = Map.empty,
        generatedAt = None,
        shouldNotify = true,
        recipients = ActivityRecipients.empty
      )

  }

  object activity {

    // Maybe this should be in the main code?
    def fromPrototype(id: String, activity: ActivityPrototype) = Activity(
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

}
