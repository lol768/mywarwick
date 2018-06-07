package controllers

import java.lang.reflect.Method

import services.{Features, FeaturesService}
import utils.JavaProxy
import warwick.sso.User

class MockFeaturesService extends FeaturesService {
  override def get(user: Option[User]): Features =
    JavaProxy[Features] { (_: Any, _: Method, _: Array[AnyRef]) =>
      java.lang.Boolean.FALSE
    }
}
