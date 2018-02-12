package controllers.publish

import helpers.{BaseSpec, Fixtures}
import models._

class ControllerHelperTest extends BaseSpec {

  "Controller helper" should {
    "filter non-api activities with nonApiActivities" in {

      val activitySave0 = Fixtures.activitySave.submissionDue
      val activitySave1 = Fixtures.activitySave.submissionDueWithoutUrl
      val activitySave2 = Fixtures.activitySave.activityFromApi

      val activityProvider: ActivityProvider = ActivityProvider(
        "id",
        true,
        Some("cool")
      )

      val activityType = ActivityType("type", Some("cool type"))

      def activityRenderWithAudienceFromSave(save: ActivitySave): ActivityRenderWithAudience = {
        new ActivityRenderWithAudience(
          Fixtures.activity.fromSave("1", save),
          Option.empty,
          Seq(),
          activityProvider,
          activityType,
          Fixtures.user.makeFoundUser(),
          AudienceSize.Public,
          Seq(),
          1
        )
      }

      val activities: Seq[ActivityRenderWithAudience] = Seq(
        activityRenderWithAudienceFromSave(activitySave0),
        activityRenderWithAudienceFromSave(activitySave1),
        activityRenderWithAudienceFromSave(activitySave2)
      )

      val result = ControllerHelper.nonApiActivities(activities)
      val expected = Seq(
        activityRenderWithAudienceFromSave(activitySave0),
        activityRenderWithAudienceFromSave(activitySave1)
      )
      result mustBe expected
    }
  }

}
