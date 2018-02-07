package services

import java.lang.reflect.{InvocationHandler, Method}
import javax.inject.{Inject, Provider, Singleton}

import com.google.inject.ImplementedBy

import scala.reflect.runtime.{universe => ru}
import scala.reflect.classTag
import play.api.Configuration

import scala.reflect.ClassTag

/**
  * To work with feature flags:
  *  - Add Boolean defs here
  *  - Define them under mywarwick.features in default.conf
  *  - @Inject Features into wherever.
  */
trait Features {
  def news: Boolean
}

/**
  *
  *
  * Don't worry about everything down here...
  *
  *
  *
  */

@ImplementedBy(classOf[FeaturesServiceImpl])
trait FeaturesService extends Provider[Features] {
  def get: Features
}

@Singleton
class FeaturesServiceImpl @Inject() (config: Configuration)
  extends BooleanFlagsAccessor[Features](config.get[Configuration]("mywarwick.features"))
  with FeaturesService

/**
  * General purpose mapper from a Configuration containing only boolean items,
  * to a trait containing only boolean defs.
  */
abstract class BooleanFlagsAccessor[T : ClassTag](config: Configuration) extends Provider[T] {

  override def get: T = proxy

  // Plain old java.lang.reflect.Proxy

  private class FeaturesInvocationHandler extends InvocationHandler {
    override def invoke(o: scala.Any, method: Method, objects: Array[AnyRef]): AnyRef = {
      val keyName = method.getName
      val value = config.get[Boolean](keyName)
      Boolean.box(value)
    }
  }

  private val tClass = classTag[T].runtimeClass

  private val proxy: T = java.lang.reflect.Proxy.newProxyInstance(
      getClass.getClassLoader,
      Array(tClass),
      new FeaturesInvocationHandler
    ).asInstanceOf[T]

  private def precheck(): Unit = {
    val confKeys = config.subKeys
    val traitKeys = tClass.getDeclaredMethods.map(_.getName).toSet
    if (confKeys != traitKeys) {
      val className = tClass.getName
      throw new IllegalStateException(
        s"""Feature keys in config didn't match $className object -
           |In config but not $className: ${confKeys diff traitKeys}
           |In $className but not config: ${traitKeys diff confKeys}
         """.stripMargin)
    }
  }

  precheck()
}

