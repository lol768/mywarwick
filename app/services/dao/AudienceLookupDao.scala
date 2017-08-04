package services.dao

import javax.inject.{Inject, Named}

import org.apache.http.client.methods.HttpGet
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws._
import system.Logging
import uk.ac.warwick.sso.client.trusted.{TrustedApplicationUtils, TrustedApplicationsManager}
import warwick.sso.{UniversityID, UserLookupService, Usercode}
import LookupModule._
import play.api.libs.functional.syntax._

import scala.concurrent.Future

case class LookupModule(
  code: String,
  name: String,
  departmentName: String
)
object LookupModule {
  implicit val reads: Reads[LookupModule] = (
    (__ \ "code").read[String] and
      (__ \ "name").read[String] and
      (__ \ "department").read[String]
  )(LookupModule.apply _)
}

case class LookupSeminarGroup(
  id: String,
  name: String
)

trait AudienceLookupDao {

  def resolveDepartment(departmentCode: String): Future[Seq[Usercode]]
  def resolveTeachingStaff(departmentCode: String): Future[Seq[Usercode]]
  def resolveAdminStaff(departmentCode: String): Future[Seq[Usercode]]
  def resolveUndergraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]]
  def resolveModule(moduleCode: String): Future[Seq[Usercode]]
  def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]]
  def resolveTutees(tutorId: UniversityID): Future[Seq[Usercode]]

  def findModules(query: String): Future[Seq[LookupModule]]
  def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]]

}

@Named("tabula")
class TabulaAudienceLookupDao @Inject()(
  ws: WSClient,
  trustedApplicationsManager: TrustedApplicationsManager,
  configuration: Configuration,
  userLookupService: UserLookupService
) extends AudienceLookupDao with Logging {

  import system.ThreadPools.externalData

  private val tabulaModuleQuery = configuration.getString("mywarwick.tabula.moduleQuery")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.moduleQuery in application.conf"))

  private val tabulaUsercode = configuration.getString("mywarwick.tabula.user")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.tabula.user in application.conf"))

  override def resolveDepartment(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveTeachingStaff(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveAdminStaff(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveUndergraduates(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveTaughtPostgraduates(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveResearchPostgraduates(departmentCode: String): Future[Seq[Usercode]] = ???

  override def resolveModule(moduleCode: String): Future[Seq[Usercode]] = ???

  override def resolveSeminarGroup(groupId: String): Future[Seq[Usercode]] = ???

  override def resolveTutees(tutorId: UniversityID): Future[Seq[Usercode]] = ???

  override def findModules(query: String): Future[Seq[LookupModule]] = {
    getAsJson(tabulaModuleQuery, Seq("query" -> query))
      .map(_.validate[Seq[LookupModule]].fold(
        invalid => {
          logger.error(s"Could not parse JSON result from Tabula: $invalid")
          Seq()
        },
        modules => modules
      ))
  }

  override def findSeminarGroups(query: String): Future[Seq[LookupSeminarGroup]] = ???


  private def getAsJson(url: String, params: Seq[(String, String)]): Future[JsValue] = {
    ws.url(url).withQueryString(params:_*).get().map(response => response.json)
  }

  private def setupRequest(url: String): WSRequest = {
    val httpRequest = new HttpGet(url)
    TrustedApplicationUtils.signRequest(trustedApplicationsManager.getCurrentApplication, tabulaUsercode, httpRequest)
    val trustedHeaders = httpRequest.getAllHeaders.map(h => h.getName -> h.getValue)

    ws
      .url(url)
      .withHeaders(trustedHeaders: _*)
  }
}
