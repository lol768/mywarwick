package services

import models.FeaturePreferences
import org.scalatestplus.play.PlaySpec
import play.api.Configuration

trait MyTestFeatures {
  def news: Boolean
  def potato: Boolean
}

class MyTestAccessor(c: Configuration, prefs: FeaturePreferences)
  extends FlagsAccessor[MyTestFeatures](c, prefs)

class FlagsAccessorTest extends PlaySpec {
  "FlagsAccessor" should {
    "work" in {
      val features = new MyTestAccessor(Configuration(
          "news" -> "on",
          "potato" -> "off"
      ), FeaturePreferences.empty).get
      features.news mustBe true
      features.potato mustBe false
    }

    "reject extra conf keys" in {
      intercept[IllegalStateException] {
        new MyTestAccessor(Configuration(
          "news" -> "on",
          "potato" -> "off",
          "bums" -> "on"
        ), FeaturePreferences.empty).get
      }
    }

    "reject missing conf keys" in {
      intercept[IllegalStateException] {
        new MyTestAccessor(Configuration(
          "news" -> "on"
        ), FeaturePreferences.empty).get
      }
    }
  }
}
