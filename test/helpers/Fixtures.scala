package helpers

import models.{Activity, ActivityRecipients, ActivityPrototype}

import org.joda.time.DateTime

/**
  * Prebuilt or buildable fake data models for use in tests.
  */
object Fixtures {

  object activityPrototype {

    lazy val submissionDue = ActivityPrototype(
      providerId = "tabula",
      `type` = "coursework-due",
      title = "Coursework for CS108 is due",
      text = "You need to submit this.",
      tags = Nil,
      replace = Map(),
      generatedAt = Some(new DateTime),
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
      replacedBy = None,
      generatedAt = activity.generatedAt.getOrElse(DateTime.now),
      createdAt = DateTime.now,
      shouldNotify = activity.shouldNotify
    )

  }

}
