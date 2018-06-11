package controllers

import java.lang.reflect.Method

import services.{Features, FeaturesService}
import utils.JavaProxy
import warwick.sso.{User, Usercode}

object MockFeaturesService {
  val defaultFeatures: Features =
    JavaProxy[Features] { (_: Any, _: Method, _: Array[AnyRef]) =>
      java.lang.Boolean.FALSE
    }
}

class MockFeaturesService(features: Features = MockFeaturesService.defaultFeatures) extends FeaturesService {
  override def get(user: Option[User]): Features = features
  override def get(usercode: Usercode): Features = features
}
