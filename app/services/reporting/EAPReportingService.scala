package services.reporting

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao._
import warwick.sso.Department

@ImplementedBy(classOf[EAPReportingServiceImpl])
trait EAPReportingService {
  
  def getMembershipByType(): Map[String, Int]
  
  def getMembershipByDepartment(): Map[Option[Department], Int]
}

@Singleton
class EAPReportingServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  departmentsDao: DepartmentInfoDao,
  userPreferencesDao: UserPreferencesDao,
) extends EAPReportingService {
  
  override def getMembershipByType(): Map[String, Int] = {
    db.withConnection(implicit c => {
      userPreferencesDao.countEAPByType()
    })
  }

  override def getMembershipByDepartment(): Map[Option[Department], Int] = {
    db.withConnection(implicit c => {
      userPreferencesDao.countEAPByDepartment()
    })
  }
}
