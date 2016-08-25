package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.Audience
import models.Audience._
import warwick.sso.{GroupName, Usercode}

case class AudienceComponentSave(name: String, value: Option[String], deptCode: Option[String])

@ImplementedBy(classOf[AudienceDaoImpl])
trait AudienceDao {
  def saveAudience(audience: Audience)(implicit c: Connection): String

  def getAudience(audienceId: String)(implicit c: Connection): Audience

  def deleteAudience(audienceId: String)(implicit c: Connection)
}

class AudienceDaoImpl extends AudienceDao {

  override def saveAudience(audience: Audience)(implicit c: Connection): String = {
    val id = UUID.randomUUID().toString
    audienceToComponents(audience).map(component =>
      saveComponent(id, component)
    )
    id
  }

  override def deleteAudience(audienceId: String)(implicit c: Connection) = {
    SQL"DELETE FROM AUDIENCE_COMPONENT WHERE AUDIENCE_ID = $audienceId"
      .execute()
  }

  private def saveComponent(id: String, component: AudienceComponentSave)(implicit c: Connection) = {
    import component._
    SQL"""
        INSERT INTO audience_component (audience_id, name, value, dept_code)
        VALUES ($id, $name, $value, $deptCode)
        """.execute()
  }

  private val componentParser = {
    get[String]("NAME") ~
      get[Option[String]]("VALUE") ~
      get[Option[String]]("DEPT_CODE") map {
      case name ~ value ~ deptCode =>
        AudienceComponentSave(name, value, deptCode)
    }
  }

  override def getAudience(audienceId: String)(implicit c: Connection): Audience =
    audienceFromComponents(
      SQL"SELECT * FROM audience_component WHERE audience_id=$audienceId".as(componentParser.*)
    )

  def audienceFromComponents(components: Seq[AudienceComponentSave]): Audience = Audience(
    components.groupBy(_.deptCode).flatMap {
      case (None, components) => components.groupBy(_.name).flatMap {
        case ("Public", _) => Seq(PublicAudience)
        case ("Module", components) => components.collect {
          case AudienceComponentSave("Module", Some(code), _) => ModuleAudience(code)
        }
        case ("Webgroup", components) => components.collect {
          case AudienceComponentSave("Webgroup", Some(group), _) => WebgroupAudience(GroupName(group))
        }
        case ("Usercode", components) => components.flatMap(_.value).map(Usercode).map(UsercodeAudience.apply)
        case (_, components) => components.map(_.name).flatMap(ComponentParameter.unapply)
      }
      case (Some(deptCode), components) =>
        val subsets = components.map(_.name).map {
          case DepartmentSubset(c) => c
        }
        Seq(DepartmentAudience(deptCode, subsets))
    }.toSeq
  )

  def audienceToComponents(audience: Audience): Seq[AudienceComponentSave] =
    audience.components.flatMap {
      case PublicAudience => Seq(AudienceComponentSave("Public", None, None))
      case ds: DepartmentSubset => resolveSubset(None, ds)
      case DepartmentAudience(code, subsets) => subsets.flatMap { subset => resolveSubset(Some(code), subset) }
      case ModuleAudience(code) => Seq(AudienceComponentSave("Module", Some(code), None))
      case WebgroupAudience(group) => Seq(AudienceComponentSave("Webgroup", Some(group.string), None))
      case UsercodeAudience(usercode) => Seq(AudienceComponentSave("Usercode", Some(usercode.string), None))
    }

  private def resolveSubset(deptCode: Option[String], subset: DepartmentSubset): Seq[AudienceComponentSave] =
    subset match {
      case All => Seq(AudienceComponentSave("All", None, deptCode))
      case Staff => Seq(AudienceComponentSave("Staff", None, deptCode))
      case UndergradStudents => Seq(AudienceComponentSave("UndergradStudents", None, deptCode))
      case TaughtPostgrads => Seq(AudienceComponentSave("TaughtPostgrads", None, deptCode))
      case ResearchPostgrads => Seq(AudienceComponentSave("ResearchPostgrads", None, deptCode))
      case TeachingStaff => Seq(AudienceComponentSave("TeachingStaff", None, deptCode))
    }

}
