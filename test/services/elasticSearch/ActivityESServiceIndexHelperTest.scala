package services.elasticSearch

import java.util.Date

import helpers.BaseSpec
import org.scalatest.mockito.MockitoSugar

class ActivityESServiceIndexHelperTest extends BaseSpec with MockitoSugar {

  class Scope {

  }

  "ActivityESServiceIndexHelper" should {

    "make index request builder correctly" in new Scope {

      val activityDoc = ActivityDocument(
        "test1",
        "test2",
        "test3",
        "test4",
        "test5",
        "test6",
        new Date(0),
        "test1",
        Seq("component1", "component2"),
        Seq("user1", "user2")
      )

      val result = ActivityESServiceIndexHelper.makeIndexDocBuilder(activityDoc)

      result.string() must be ("""[{"provider_id":"test1","activity_type":"test2","title":"test3","url":"test4","text":"test5","replaced_by":"test6","published_at":"1970-01-01T00:00:00.000Z","publisher":"test1","resolved_users":["user1","user2"],"audience_components":["component1","component2"]}]""")

    }

  }

}
