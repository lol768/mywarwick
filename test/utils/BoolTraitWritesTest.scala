package utils

import helpers.BaseSpec
import play.api.libs.json.Json

trait TestTrait {
  def news: Boolean
  def trews: Boolean
}

class TestTraitImpl extends TestTrait {
  def news = true
  def trews = false
}

class BoolTraitWritesTest extends BaseSpec {

  "BoolTraitWritesTest" should {

    "write to JSON" in {
      val o: TestTrait = new TestTraitImpl()
      val json = Json.toJson(o)(new BoolTraitWrites[TestTrait])
      json mustBe Json.obj(
        "news" -> true,
        "trews" -> false
      )
    }

  }
}
