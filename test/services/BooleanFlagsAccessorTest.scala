package services

import org.scalatestplus.play.PlaySpec
import play.api.Configuration

trait MyTestFeatures {
  def news: Boolean
  def potato: Boolean
}

class MyTestAccessor(c: Configuration) extends BooleanFlagsAccessor[MyTestFeatures](c)

class BooleanFlagsAccessorTest extends PlaySpec {
  "BooleanFlagsAccessor" should {
    "work" in {
      val features = new MyTestAccessor(Configuration(
          "news" -> true,
          "potato" -> false
      )).get
      features.news mustBe true
      features.potato mustBe false
    }

    "reject extra conf keys" in {
      intercept[IllegalStateException] {
        new MyTestAccessor(Configuration(
          "news" -> true,
          "potato" -> false,
          "bums" -> true
        )).get
      }
    }

    "reject missing conf keys" in {
      intercept[IllegalStateException] {
        new MyTestAccessor(Configuration(
          "news" -> true
        )).get
      }
    }
  }
}
