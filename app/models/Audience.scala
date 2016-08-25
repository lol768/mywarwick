package models

import enumeratum.EnumEntry
import warwick.sso.{GroupName, Usercode}

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

  // Pieces of audience
  sealed trait Component extends EnumEntry
  // Pieces of department
  sealed trait DepartmentSubset extends Component

  case object PublicAudience extends Component

  case class UsercodeAudience(usercode: Usercode) extends Component

  case class WebgroupAudience(groupName: GroupName) extends Component
  case class ModuleAudience(moduleCode: String) extends Component
  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component

  case object All extends DepartmentSubset
  case object Staff extends DepartmentSubset
  case object TeachingStaff extends DepartmentSubset
  case object UndergradStudents extends DepartmentSubset
  case object TaughtPostgrads extends DepartmentSubset
  case object ResearchPostgrads extends DepartmentSubset

  val moduleCodeRegex = "Module:(.+)".r

  object ComponentParameter {
    def unapply(paramValue: String): Option[Component] = paramValue match {
      case "Staff" => Some(Staff)
      case "TeachingStaff" => Some(TeachingStaff)
      case "UndergradStudents" => Some(UndergradStudents)
      case "TaughtPostgrads" => Some(TaughtPostgrads)
      case "ResearchPostgrads" => Some(ResearchPostgrads)

      // FIXME: not handling Module or Webgroup parameters
      case moduleCodeRegex(code) => None
      case _ => ???
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
