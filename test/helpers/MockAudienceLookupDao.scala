package helpers

import models.Audience
import models.Audience.UndergradStudents
import services.dao.{AudienceLookupDao, LookupModule, LookupRelationshipType, LookupSeminarGroup}
import warwick.sso.{UniversityID, User, Usercode}

import scala.concurrent.Future

class MockAudienceLookupDao extends AudienceLookupDao {
  override def resolveDepartment(departmentCode: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveUndergraduatesInDept(departmentCode: String, level: UndergradStudents): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveUndergraduatesUniWide(level: UndergradStudents): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveModule(moduleCode: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveRelationship(agentId: UniversityID, relationshipType: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def getSeminarGroupById(groupId: String): Future[Option[LookupSeminarGroup]] = Future.successful(None)
  override def findModules(query: String): Future[Seq[LookupModule]] = Future.successful(Nil)
  override def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]] = Future.successful(Nil)
  override def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]] = Future.successful(Map.empty)
  override def resolveStaff(departmentCode: String): Future[Seq[Usercode]] = Future.successful(Nil)
  override def resolveResidence(residence: Audience.Residence): Future[Seq[Usercode]] = Future.successful(Nil)
}
