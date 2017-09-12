package services.elasticSearch

import helpers.BaseSpec
import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar


class ActivityESServiceHelperTest extends BaseSpec with MockitoSugar {

  class Scope {

  }

  "ActivityESServiceHelper" should {

    "produce correct monthly index name for alerts and activity" in new Scope {

      ActivityESServiceGetHelper.indexNameToday() must be ("alert_" +DateTime.now().toString("yyyy_MM"))

      ActivityESServiceGetHelper.indexNameToday(true) must be ("alert_" +DateTime.now().toString("yyyy_MM"))

      ActivityESServiceGetHelper.indexNameToday(false) must be
      "activity_" +DateTime.now().toString("yyyy_MM")

    }

    "produce correct all time index name for alerts and activity" in new Scope {

      ActivityESServiceGetHelper.indexNameForAllTime() must be ("alert*")

      ActivityESServiceGetHelper.indexNameForAllTime(false) must be ("activity*")

    }
  }

}
