package models

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}


class TileLayoutTest extends PlaySpec {

  "TileLayout" should {
    // TODO could check that TileInstances are passed through okay.

    "read from JSON" in {
      Json.fromJson[TileLayout](Json.arr()) must be (JsSuccess(TileLayout(Nil)))
    }

    "write to JSON" in {
      Json.toJson(TileLayout(Nil)) must be (Json.arr())
    }
  }


}