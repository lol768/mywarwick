package controllers

import java.lang.reflect.{InvocationHandler, Method}
import java.util

import services.{Features, FeaturesService}
import warwick.sso.User

class MockFeaturesService extends FeaturesService {
  override def get(user: Option[User]): Features =
    java.lang.reflect.Proxy.newProxyInstance(
      getClass.getClassLoader,
      Array(classOf[Features]),
      (o: scala.Any, method: Method, objects: Array[AnyRef]) => java.lang.Boolean.FALSE
    ).asInstanceOf[Features]
}
