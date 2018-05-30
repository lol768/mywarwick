package services

import java.lang.reflect.{InvocationHandler, Method}

import javax.inject.{Inject, Provider, Singleton}
import com.google.inject.ImplementedBy
import enumeratum.EnumEntry
import models.FeaturePreferences
import models.Platform.findValues

import scala.reflect.runtime.{universe => ru}
import scala.reflect.classTag
import play.api.Configuration
import warwick.sso.User

import scala.reflect.ClassTag
import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.{JsObject, Json, OWrites}

import scala.collection.immutable

/**
  * To work with feature flags:
  *  - Add Boolean defs here
  *  - Define them under mywarwick.features in default.conf as a FeatureState value
  *  - @Inject FeaturesService into wherever.
  */
trait Features {
  def news: Boolean
  def updateTileEditUI : Boolean
}

object Features {
  implicit val writes: OWrites[Features] = new BoolTraitWrites[Features]
}


sealed abstract class FeatureState extends EnumEntry {
  def resolve(prefs: FeaturePreferences): Boolean
}
object FeatureState extends Enum[FeatureState] {
  val values: immutable.IndexedSeq[FeatureState] = findValues
  case object on extends FeatureState {
    override def resolve(prefs: FeaturePreferences) = true
  }
  case object off extends FeatureState {
    override def resolve(prefs: FeaturePreferences) = false
  }
  case object eap extends FeatureState {
    override def resolve(prefs: FeaturePreferences): Boolean = prefs.eap
  }
}


@ImplementedBy(classOf[FeaturesServiceImpl])
trait FeaturesService {
  def get(user: Option[User]): Features
}

@Singleton
class FeaturesServiceImpl @Inject() (
  config: Configuration,
  userPreferences: UserPreferencesService
) extends FeaturesService {
  private val featuresConfig = config.get[Configuration]("mywarwick.features")

  override def get(user: Option[User]): Features = {
    val featurePreferences = user.map { user =>
      userPreferences.getFeaturePreferences(user.usercode)
    }.getOrElse {
      FeaturePreferences.empty
    }
    new FlagsAccessor[Features](featuresConfig, featurePreferences).get
  }
}

/**
  * General purpose mapper from a Configuration containing only boolean items,
  * to a trait containing only boolean defs.
  */
class FlagsAccessor[T : ClassTag](config: Configuration, prefs: FeaturePreferences) extends Provider[T] {

  override def get: T = proxy

  // Plain old java.lang.reflect.Proxy

  private val allowedValues: Set[String] = FeatureState.namesToValuesMap.keySet

  private class FeaturesInvocationHandler extends InvocationHandler {
    override def invoke(o: scala.Any, method: Method, objects: Array[AnyRef]): AnyRef = {
      val keyName = method.getName
      val value = config.getAndValidate[String](keyName, allowedValues)
      val state: FeatureState = FeatureState.withName(value)
      Boolean.box(state.resolve(prefs))
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
    val traitKeys = tClass.getDeclaredMethods.filter(m => m.getReturnType == classOf[Boolean]).map(_.getName).toSet
    if (confKeys != traitKeys) {
      val className = tClass.getName
      throw new IllegalStateException(
        s"""Feature keys in config didn't match $className object -
           |In config but not $className: ${confKeys diff traitKeys}
           |In $className but not config: ${traitKeys diff confKeys}
         """.stripMargin)
    }

    for (keyName <- config.subKeys) {
      config.getAndValidate[String](keyName, allowedValues)
    }
  }

  precheck()
}

