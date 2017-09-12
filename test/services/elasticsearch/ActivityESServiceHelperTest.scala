package services.elasticsearch

import java.util.Date

import helpers.BaseSpec
import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import uk.ac.warwick.util.core.jodatime.DateTimeUtils
import uk.ac.warwick.util.core.jodatime.DateTimeUtils.Callback


class ActivityESServiceHelperTest extends BaseSpec with MockitoSugar {

  class Scope {

  }

  "ActivityESServiceHelper" should {

    "produce correct monthly index name for alerts and activity" in new Scope {

      DateTimeUtils.useMockDateTime(DateTime.parse("2016-06-30T01:20"), new Callback {
        override def doSomething() = {

          ActivityESServiceGetHelper.indexNameToday(true, DateTime.now().toString("yyyy_MM")) must be("alert_2016_06")

          ActivityESServiceGetHelper.indexNameToday(false, DateTime.now().toString("yyyy_MM")) must be("activity_2016_06")
        }
      })


    }

    "produce correct all time index name for alerts and activity" in new Scope {

      ActivityESServiceGetHelper.indexNameForAllTime() must be("alert*")

      ActivityESServiceGetHelper.indexNameForAllTime(false) must be("activity*")

    }
  }

  "ActivityESServiceHelper" should {

    "make elasticsearch document builder from activity document correctly" in new Scope {

      val activityDoc = ActivityDocument(
        "test1",
        "test2",
        "test3",
        "test4",
        "test5",
        "test6",
        new DateTime(0),
        "test1",
        Seq("component1", "component2"),
        Seq("user1", "user2")
      )

      val helper = ActivityESServiceHelper
      val result = helper.elasticSearchContentBuilderFromActivityDocument(activityDoc)

      result.string() must be("""{"provider_id":"test1","activity_type":"test2","title":"test3","url":"test4","text":"test5","replaced_by":"test6","published_at":"1970-01-01T00:00:00.000Z","publisher":"test1","resolved_users":["user1","user2"],"audience_components":["component1","component2"]}""")

    }

  }

}
