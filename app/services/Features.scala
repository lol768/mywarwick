package services

import java.lang.reflect.Method

import com.google.inject.ImplementedBy
import enumeratum.{Enum, EnumEntry}
import javax.inject.{Inject, Provider, Singleton}
import models.FeaturePreferences
import play.api.Configuration
import play.api.libs.json.OWrites
import utils.{BoolTraitWrites, JavaProxy}
import warwick.sso.{User, Usercode}

import scala.collection.immutable
import scala.reflect.{ClassTag, classTag}

/**
  * To work with feature flags:
  *  - Add Boolean defs here
  *  - Define them under mywarwick.features in default.conf as a FeatureState value
  *  - @Inject FeaturesService into wherever.
  */
trait Features {
  def news: Boolean
  def updateTileEditUI : Boolean
  def doNotDisturb : Boolean
  def eap: Boolean
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
  def get(usercode: Usercode): Features
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

  override def get(usercode: Usercode): Features = {
    new FlagsAccessor[Features](featuresConfig, userPreferences.getFeaturePreferences(usercode)).get
  }
}

/**
  * General purpose mapper from a Configuration containing only FeatureState values,
  * to a trait containing only boolean defs.
  *
  * @param config the Configuration to read from
  * @param prefs prefs derived for the current user (based on their EAP opt-in selection) - determines
  *              how things are resolved to a boolean.
  */
class FlagsAccessor[T : ClassTag](config: Configuration, prefs: FeaturePreferences) extends Provider[T] {

  override def get: T = proxy

  private val allowedValues: Set[String] = FeatureState.namesToValuesMap.keySet

  private val tClass = classTag[T].runtimeClass

  // Plain old java.lang.reflect.Proxy
  private val proxy: T = JavaProxy[T]{ (_: Any, method: Method, _: Array[AnyRef]) =>
    val keyName = method.getName
    val value = config.getAndValidate[String](keyName, allowedValues)
    val state: FeatureState = FeatureState.withName(value)
    Boolean.box(state.resolve(prefs))
  }

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

    // throw if any values are invalid
    for (keyName <- config.subKeys) {
      config.getAndValidate[String](keyName, allowedValues)
    }
  }

  precheck()
}

