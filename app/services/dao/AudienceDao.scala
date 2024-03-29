package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.Audience
import models.Audience._
import warwick.sso.{GroupName, UniversityID, Usercode}

case class AudienceComponentSave(name: String, value: Option[String], deptCode: Option[String])

object AudienceComponentSave {
  def fromCompoundValue(name: String, valueList: Seq[String], deptCode: Option[String]): AudienceComponentSave = {
    if (valueList.isEmpty) {
      AudienceComponentSave(name, None, deptCode)
    } else {
      AudienceComponentSave(name, Some(valueList.mkString("|")), deptCode)
    }
  }

  def toCompoundValue2(compoundValue: String): (String, String) =
    Seq(compoundValue.split('|'): _*) match {
      case (value1 :: value2 :: Nil) => (value1, value2)
      case _ => throw new IllegalArgumentException(s"Expected 2 values but was ${compoundValue.split('|').length}")
    }
}

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

  private def resolveToSubset(group: (String, Seq[AudienceComponentSave])): Seq[Component] =
    group match {
      case ("Module", components) => components.collect {
        case AudienceComponentSave("Module", Some(code), _) => ModuleAudience(code)
      }
      case ("SeminarGroup", components) => components.collect {
        case AudienceComponentSave("SeminarGroup", Some(groupId), _) => SeminarGroupAudience(groupId)
      }
      case ("Relationship", components) => components.collect {
        case AudienceComponentSave("Relationship", Some(compoundValue), _) =>
          AudienceComponentSave.toCompoundValue2(compoundValue) match {
            case (relationshipType, universityIdString) => RelationshipAudience(relationshipType, UniversityID(universityIdString))
          }
      }
      case ("Usercode", components) => Seq(UsercodesAudience(components.flatMap(_.value).map(Usercode).toSet))
      case (_, components) => components.map(c =>
        if (c.value.isDefined) s"${c.name}:${c.value.get}"
        else c.name
      ).flatMap(ComponentParameter.unapply)
    }

  def audienceFromComponents(audienceComponents: Seq[AudienceComponentSave]): Audience = Audience(
    audienceComponents.groupBy(_.deptCode).flatMap {
      case (None, groupedComponents) => groupedComponents.groupBy(_.name).flatMap {
        case ("Public", _) => Seq(PublicAudience)
        case ("WebGroup", components) => components.collect {
          case AudienceComponentSave("WebGroup", Some(group), _) => WebGroupAudience(GroupName(group))
        }
        case ("HallsOfResidence", components) =>
          components.flatMap(_.value).map(Residence.fromId).map(ResidenceAudience)
        case (group) => resolveToSubset(group)
      }
      case (Some(deptCode), components) =>
        val subsets = components.groupBy(_.name).flatMap {
          case ("All", _) => DepartmentSubset.unapply("All")
          case (group) => resolveToSubset(group).flatMap {
            case ds: DepartmentSubset => Some(ds)
            case _ => None
          }
        }.toSeq
        Seq(DepartmentAudience(deptCode, subsets))
    }.toSeq
  )

  def audienceToComponents(audience: Audience): Seq[AudienceComponentSave] =
    audience.components.flatMap {
      case PublicAudience => Seq(AudienceComponentSave("Public", None, None))
      case ds: DepartmentSubset => resolveSubset(None, ds)
      case DepartmentAudience(code, subsets) => subsets.flatMap { subset => resolveSubset(Some(code), subset) }
      case WebGroupAudience(group) => Seq(AudienceComponentSave("WebGroup", Some(group.string), None))
      case optIn: OptIn => Seq(AudienceComponentSave(s"OptIn:${optIn.optInType}", Some(optIn.optInValue), None))
      case residenceAudience: ResidenceAudience => Seq(AudienceComponentSave(s"HallsOfResidence", Some(residenceAudience.residence.id), None))
    }

  private def resolveSubset(deptCode: Option[String], subset: DepartmentSubset): Seq[AudienceComponentSave] =
    subset match {
      case All => Seq(AudienceComponentSave("All", None, deptCode))
      case Staff => Seq(AudienceComponentSave("Staff", None, deptCode))
      case ug: UndergradStudents => Seq(AudienceComponentSave("UndergradStudents", Some(ug.value), deptCode))
      case TaughtPostgrads => Seq(AudienceComponentSave("TaughtPostgrads", None, deptCode))
      case ResearchPostgrads => Seq(AudienceComponentSave("ResearchPostgrads", None, deptCode))
      case TeachingStaff => Seq(AudienceComponentSave("TeachingStaff", None, deptCode))
      case AdminStaff => Seq(AudienceComponentSave("AdminStaff", None, deptCode))
      case ModuleAudience(code) => Seq(AudienceComponentSave("Module", Some(code), deptCode))
      case SeminarGroupAudience(groupId) => Seq(AudienceComponentSave("SeminarGroup", Some(groupId), deptCode))
      case RelationshipAudience(relationshipType, agentId) => Seq(AudienceComponentSave.fromCompoundValue("Relationship", Seq(relationshipType, agentId.string), deptCode))
      case UsercodesAudience(usercodes) => usercodes.map(usercode => AudienceComponentSave("Usercode", Some(usercode.string), deptCode)).toSeq
    }
}
