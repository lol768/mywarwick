package models.news

import warwick.sso.GroupName

case class Audience(components: Seq[Audience.Component] = Nil)

object Audience {
  // Pieces of audience
  sealed trait Component
  // Pieces of department
  trait DepartmentSubset
  case class WebgroupAudience(groupName: GroupName) extends Component
  case class ModuleAudience(moduleCode: String) extends Component
  case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends Component
  //case object AdminStaff extends Component
  //case object UndergradStudentsFirstYear extends Component
  //case object UndergradStudentsFinalYear extends Component
  case object Staff extends DepartmentSubset
  case object TeachingStaff extends DepartmentSubset
  case object UndergradStudents extends Component with DepartmentSubset
  case object TaughtPostgrads extends Component with DepartmentSubset
  case object ResearchPostgrads extends Component with DepartmentSubset
}