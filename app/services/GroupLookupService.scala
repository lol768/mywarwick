package services

import com.google.inject.{ImplementedBy, Inject}
import com.google.inject.name.Named
import services.dao.{AudienceLookupDao, LookupModule, LookupRelationshipType, LookupSeminarGroup}
import warwick.sso.{UniversityID, User}

import scala.concurrent.Future

@ImplementedBy(classOf[GroupLookupServiceImpl])
trait GroupLookupService {
  def findModule(query: String): Future[Seq[LookupModule]]

  def findSeminarGroup(query: String): Future[Seq[LookupSeminarGroup]]

  def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]]
}

class GroupLookupServiceImpl @Inject()(
  @Named("tabula") audienceLookupDao: AudienceLookupDao
) extends GroupLookupService {

  def findModule(query: String): Future[Seq[LookupModule]] =
    audienceLookupDao.findModules(query)

  def findSeminarGroup(query: String): Future[Seq[LookupSeminarGroup]] =
    audienceLookupDao.findSeminarGroups(query)

  def findRelationships(agentId: UniversityID): Future[Map[LookupRelationshipType, Seq[User]]] =
    audienceLookupDao.findRelationships(agentId)
}
