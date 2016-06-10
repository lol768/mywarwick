package services.dao

import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.news.Audience
import models.news.Audience._
import play.api.db.Database
import warwick.sso.GroupName

case class AudienceComponentSave(name: String, value: Option[String], deptCode: Option[String])

@ImplementedBy(classOf[AudienceDaoImpl])
trait AudienceDao {
  def saveAudience(audience: Audience): String
  def getAudience(audienceId: String): Audience
}

class AudienceDaoImpl @Inject()(
  db: Database
) extends AudienceDao {

  override def saveAudience(audience: Audience): String = {
    val id = UUID.randomUUID().toString
    audienceToComponents(audience).map(component =>
      saveComponent(id, component)
    )
    id
  }

  private def saveComponent(id: String, component: AudienceComponentSave) = {
    import component._
    db.withConnection { implicit c =>
      SQL"""
        INSERT INTO audience_component (id, name, value, dept_code)
        VALUES ($id, $name, $value, $deptCode)
        """.execute()
    }
  }

  private val componentParser = {
    get[String]("NAME") ~
      get[Option[String]]("VALUE") ~
      get[Option[String]]("DEPT_CODE") map {
      case name ~ value ~ deptCode =>
        AudienceComponentSave(name, value, deptCode)
    }
  }

  override def getAudience(audienceId: String): Audience =
    audienceFromComponents(
      db.withConnection { implicit c =>
        SQL"""
       SELECT * FROM audience_component WHERE id=$audienceId
       """.as(componentParser.*)
      }
    )

  def audienceFromComponents(components: Seq[AudienceComponentSave]): Audience = Audience(
    components.groupBy(_.deptCode).flatMap {
      case (None, components) => components.flatMap {
        case AudienceComponentSave("Module", Some(code), _) => Some(ModuleAudience(code))
        case AudienceComponentSave("Webgroup", Some(group), _) => Some(WebgroupAudience(GroupName(group)))
        case other => ComponentParameter.unapply(other.name)
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
      case ds: DepartmentSubset => resolveSubset(None, ds)
      case DepartmentAudience(code, subsets) => subsets.flatMap { subset => resolveSubset(Some(code), subset) }
      case ModuleAudience(code) => Seq(AudienceComponentSave("Module", Some(code), None))
      case WebgroupAudience(group) => Seq(AudienceComponentSave("Webgroup", Some(group.string), None))
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
