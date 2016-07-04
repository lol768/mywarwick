package services.dao

import java.sql.Connection
import java.util.regex.Pattern

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}

@ImplementedBy(classOf[ActivityTypeDaoImpl])
trait ActivityTypeDao {

  def isValidActivityType(name: String)(implicit c: Connection): Boolean

  def isValidActivityTagName(name: String)(implicit c: Connection): Boolean

  def getValueValidationRegex(name: String)(implicit c: Connection): Option[String]

}

@Singleton
class ActivityTypeDaoImpl extends ActivityTypeDao {

  override def isValidActivityType(name: String)(implicit c: Connection) =
    SQL"SELECT COUNT(*) FROM ACTIVITY_TYPE WHERE name = $name"
      .executeQuery()
      .as(scalar[Int].single) == 1

  override def isValidActivityTagName(name: String)(implicit c: Connection) =
    SQL"SELECT COUNT(*) FROM ACTIVITY_TAG_TYPE WHERE name = $name"
      .executeQuery()
      .as(scalar[Int].single) == 1

  override def getValueValidationRegex(name: String)(implicit c: Connection) =
    SQL"SELECT VALUE_VALIDATION_REGEX FROM ACTIVITY_TAG_TYPE WHERE name = $name"
      .executeQuery()
      .as(scalar[String].singleOpt)

}
