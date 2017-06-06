package models

import enumeratum.EnumEntry
import warwick.sso.{GroupName, Usercode}

import scala.util.matching.Regex

case class Audience(components: Seq[Audience.Component] = Nil) {
  val public = components.contains(Audience.PublicAudience)

  if (public && components.length > 1) {
    throw new IllegalArgumentException("Public audience can't have any other components")
  }
}

object Audience {
  val Public = Audience(Seq(PublicAudience))

  def usercodes(usercodes: Seq[Usercode]): Audience = {
    Audience(usercodes.map(UsercodeAudience))
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
  sealed trait DepartmentSubset extends Component

  case object PublicAudience extends Component

  case class UsercodeAudience(usercode: Usercode) extends Component

  case class WebGroupAudience(groupName: GroupName) extends Component
  case class ModuleAudience(moduleCode: String) extends Component
  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component

  case object All extends DepartmentSubset
  case object Staff extends DepartmentSubset
  case object TeachingStaff extends DepartmentSubset
  case object UndergradStudents extends DepartmentSubset
  case object TaughtPostgrads extends DepartmentSubset
  case object ResearchPostgrads extends DepartmentSubset

  val moduleCodeRegex: Regex = "^Module:(.+)".r
  val webGroupRegex: Regex = "^WebGroup:(.+)".r

  object ComponentParameter {
    def unapply(paramValue: String): Option[Component] = paramValue match {
      case "Staff" => Some(Staff)
      case "TeachingStaff" => Some(TeachingStaff)
      case "UndergradStudents" => Some(UndergradStudents)
      case "TaughtPostgrads" => Some(TaughtPostgrads)
      case "ResearchPostgrads" => Some(ResearchPostgrads)
      case webGroupRegex(webGroup) => Some(WebGroupAudience(GroupName(webGroup)))
      case moduleCodeRegex(code) => Some(ModuleAudience(code))
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
