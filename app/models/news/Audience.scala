package models.news

/**
  * Currently unused.
  */

trait Audience
case class CombinedAudience(components: Seq[Audience])

// Pieces of audience
trait AudienceComponent
// Pieces of department
trait DepartmentSubset
case class WebgroupAudience(groupName: String) extends AudienceComponent
case class ModuleAudience(moduleCode: String) extends AudienceComponent
case class DepartmentAudience(deptCode: String, subset: Seq[DepartmentSubset]) extends AudienceComponent
case object TeachingStaff extends AudienceComponent
case object AdminStaff extends AudienceComponent
case object Staff extends DepartmentSubset
case object UndergradStudents extends AudienceComponent with DepartmentSubset
case object UndergradStudentsFirstYear extends AudienceComponent
case object UndergradStudentsFinalYear extends AudienceComponent
case object TaughtPostgrads extends AudienceComponent with DepartmentSubset
case object ResearchPostgrads extends AudienceComponent with DepartmentSubset