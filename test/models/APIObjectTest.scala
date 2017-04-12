package models

import models.API.Error
import helpers.BaseSpec
import play.api.libs.json.{JsError, Json}

object APIObjectTest {
  case class ExampleData(items: Seq[String])
}

class APIObjectTest extends BaseSpec {
  import APIObjectTest.ExampleData

  implicit val dataFormat = Json.format[ExampleData]

  "API" should {
    "deserialise a simple Success value" in {
      Json.obj(
        "success" -> true,
        "status" -> "great",
        "data" -> "Honk honk"
      ).as[API.Response[String]] must be (API.Success[String]("great", "Honk honk"))
    }

    "deserialize a complex Success value" in {
      Json.obj(
        "success" -> true,
        "status" -> "great",
        "data" -> Map(
          "items" -> Seq("eggs","ham")
        )
      ).as[API.Response[ExampleData]] must be (API.Success("great", ExampleData(Seq("eggs","ham"))))
    }

    "serialize Success to JSON" in {
      Json.toJson(API.Success("alright", ExampleData(Seq("potato")))) must be (Json.obj(
        "success" -> true,
        "status" -> "alright",
        "data" -> Map(
          "items" -> Seq("potato")
        )
      ))
    }

    "deserialize Failure" in {
      Json.obj(
        "success" -> false,
        "status" -> "real_bad",
        "errors" -> Nil
      ).as[API.Response[ExampleData]] must be (API.Failure("real_bad", Nil))

      Json.obj(
        "success" -> false,
        "status" -> "real_bad",
        "errors" -> Seq(
          Map("id" -> "permdenied", "message" -> "No")
        )
      ).as[API.Response[ExampleData]] must be (API.Failure("real_bad", Seq(Error("permdenied","No"))))
    }

    "reject missing properties" in {
      Json.obj(
        "status" -> "shrug",
        "errors" -> Nil
      ).validate[API.Response[ExampleData]] must be (a [JsError])
    }

  }
}
