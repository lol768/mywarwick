package models

import enumeratum.EnumEntry
import warwick.sso.{GroupName, UniversityID, Usercode}

import scala.util.matching.Regex

case class Audience(components: Seq[Audience.Component] = Nil) {
  val public: Boolean = components.contains(Audience.PublicAudience)

  if (public && components.length > 1) {
    throw new IllegalArgumentException("Public audience can't have any other components")
  }
}

object Audience {
  val Public = Audience(Seq(PublicAudience))

  def usercodes(usercodes: Seq[Usercode]): Audience = {
    Audience(usercodes match {
      case _::_ => Seq(UsercodesAudience(usercodes))
      case _ => Nil
    })
  }

  def usercode(usercode: Usercode): Audience = {
    usercodes(Seq(usercode))
  }

  def webGroup(groupName: GroupName): Audience = {
    Audience(Seq(WebGroupAudience(groupName)))
  }

  // Pieces of audience
  sealed trait Component extends EnumEntry

  // Pieces of department
  sealed trait DepartmentSubset extends Component {
    val displayName: String = toString
  }

  case object PublicAudience extends Component

  case class UsercodesAudience(usercodes: Seq[Usercode]) extends Component

  case class WebGroupAudience(groupName: GroupName) extends Component // No longer available in Audience Picker UI

  case class ModuleAudience(moduleCode: String) extends Component

  case class SeminarGroupAudience(groupId: String) extends Component

  case class RelationshipAudience(relationshipType: String, agentId: UniversityID) extends Component

  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component

  case object All extends DepartmentSubset

  case object Staff extends DepartmentSubset // No longer available in Audience Picker UI

  case object TeachingStaff extends DepartmentSubset {
    override val displayName = "Teaching Staff"
  }

  case object AdminStaff extends DepartmentSubset {
    override val displayName = "Administrative Staff"
  }

  case object UndergradStudents extends DepartmentSubset {
    override val displayName = "Undergraduates"
  }

  case object TaughtPostgrads extends DepartmentSubset {
    override val displayName = "Taught Postgraduates"
  }

  case object ResearchPostgrads extends DepartmentSubset {
    override val displayName = "Research Postgraduates"
  }

  sealed abstract class OptIn(val optInType: String, val optInValue: String, val description: String) extends Component

  sealed abstract class LocationOptIn(val value: String, override val description: String) extends OptIn(LocationOptIn.optInType, value, description)

  object LocationOptIn {
    val optInType = "Location"

    case object CentralCampusResidences extends LocationOptIn("CentralCampusResidences", "Central campus residences")

    case object WestwoodResidences extends LocationOptIn("WestwoodResidences", "Westwood residences")

    case object Coventry extends LocationOptIn("Coventry", "Coventry")

    case object Kenilworth extends LocationOptIn("Kenilworth", "Kenilworth")

    case object LeamingtonSpa extends LocationOptIn("LeamingtonSpa", "Leamington Spa")

    def values = Seq(CentralCampusResidences, WestwoodResidences, Coventry, Kenilworth, LeamingtonSpa)

    def fromValue(value: String): Option[OptIn] = values.find(_.value == value)
  }

  val moduleCodeRegex: Regex = "^Module:(.+)".r
  val seminarGroupRegex: Regex = "^SeminarGroup:(.+)".r
  val relationshipRegex: Regex = "^Relationship:(.+):(.+)".r
  val webGroupRegex: Regex = "^WebGroup:(.+)".r
  val optInRegex: Regex = "^OptIn:(.+):(.+)".r

  object ComponentParameter {
    def unapply(paramValue: String): Option[Component] = paramValue match {
      case "Staff" => Some(Staff)
      case "TeachingStaff" => Some(TeachingStaff)
      case "AdminStaff" => Some(AdminStaff)
      case "UndergradStudents" => Some(UndergradStudents)
      case "TaughtPostgrads" => Some(TaughtPostgrads)
      case "ResearchPostgrads" => Some(ResearchPostgrads)
      case webGroupRegex(webGroup) => Some(WebGroupAudience(GroupName(webGroup)))
      case moduleCodeRegex(code) => Some(ModuleAudience(code))
      case seminarGroupRegex(groupId) => Some(SeminarGroupAudience(groupId))
      case relationshipRegex(relationshipType, agentId) => Some(RelationshipAudience(relationshipType, UniversityID(agentId)))
      case optInRegex(optInType, optInValue) if optInType == LocationOptIn.optInType => LocationOptIn.fromValue(optInValue)
      case string if string.nonEmpty => {
        val validUsercodes: Seq[Usercode] = string.split(",").map(_.trim).flatMap { usercode =>
          if(!usercode.contains(":")) Some(Usercode(usercode))
          else None
        }
        if (validUsercodes.nonEmpty)
          Some(UsercodesAudience(validUsercodes))
        else
          None
      }
      case _ => None
    }
  }

  object DepartmentSubset {
    def unapply(paramValue: String): Option[DepartmentSubset] = paramValue match {
      case "All" => Some(All)
      case ComponentParameter(subset: DepartmentSubset) => Some(subset)
      case _ => None
    }
  }

}
