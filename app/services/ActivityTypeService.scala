package services

import java.util.regex.Pattern

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao.ActivityTypeDao

@ImplementedBy(classOf[ActivityTypeServiceImpl])
trait ActivityTypeService {

  def isValidActivityType(name: String): Boolean

  def isValidActivityTagName(name: String): Boolean

  def isValidActivityTag(name: String, value: String): Boolean

}

@Singleton
class ActivityTypeServiceImpl @Inject()(
  dao: ActivityTypeDao,
  @NamedDatabase("default") db: Database
) extends ActivityTypeService {
  override def isValidActivityType(name: String) =
    db.withConnection(implicit c => dao.isValidActivityType(name))

  override def isValidActivityTagName(name: String) =
    db.withConnection(implicit c => dao.isValidActivityTagName(name))

  override def isValidActivityTag(name: String, value: String) =
    db.withConnection(implicit c =>
      dao.getValueValidationRegex(name)
        .forall(regex => Pattern.matches(regex, value))
    )

}
