package services

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import services.dao.{DepartmentInfo, DepartmentInfoDao}

object DepartmentInfoService {
  val audienceDepartmentTypes = Set("ACADEMIC", "SERVICE")
}

@ImplementedBy(classOf[DepartmentInfoServiceImpl])
trait DepartmentInfoService {

  def allDepartments: Seq[DepartmentInfo]

  def allPublishableDepartments: Seq[DepartmentInfo]

}

@Singleton
class DepartmentInfoServiceImpl @Inject()(
  departmentInfoDao: DepartmentInfoDao
) extends DepartmentInfoService {

  def allDepartments: Seq[DepartmentInfo] =
    departmentInfoDao.allDepartments

  def allPublishableDepartments: Seq[DepartmentInfo] =
    allDepartments
      .filter(dept => DepartmentInfoService.audienceDepartmentTypes.contains(dept.`type`))
      .sortBy(_.name)

}
