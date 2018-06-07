package utils

import java.lang.reflect.InvocationHandler

import scala.reflect.{ClassTag, classTag}

/**
  * A wrapper around basic use of Java Proxy instances.
  */
object JavaProxy {
  /**
    * Creates a proxy that implements a single class/trait.
    * You can use a lambda in place of InvocationHandler.
    */
  def apply[T : ClassTag](handler: InvocationHandler): T =
    java.lang.reflect.Proxy.newProxyInstance(
      classTag[T].runtimeClass.getClassLoader,
      Array(classTag[T].runtimeClass),
      handler
    ).asInstanceOf[T]
}
