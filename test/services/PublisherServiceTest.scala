package services

import helpers.BaseSpec
import models.publishing.PublishingRole.Viewer
import models.publishing.{Publisher, PublisherPermission, PublishingRole}
import org.mockito.ArgumentMatchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import services.dao.PublisherDao
import warwick.sso.Usercode

class PublisherServiceTest extends BaseSpec with MockitoSugar {

  class Scope {
    protected val publisherDao: PublisherDao = mock[PublisherDao]

    val service = new PublisherServiceImpl(
      publisherDao,
      new MockDatabase()
    )
  }

  "PublisherServiceTest" should {

    "show all publishers if user has global department permission" in {
      val globalPublisher = Publisher("123","Global Publisher")
      val normalPublisher = Publisher("234","Normal Publisher")

      new Scope {
        when(publisherDao.getPublishersForUser(isEq(Usercode("cusfal")))(any())).thenReturn(Seq(globalPublisher))
        when(publisherDao.getPublisherDepartments(isEq(globalPublisher.id))(any())).thenReturn(Seq(PublisherService.AllDepartmentsWildcard))
        when(publisherDao.all(any())).thenReturn(Seq(globalPublisher, normalPublisher))

        private val result = service.getPublishersForUser(Usercode("cusfal"))
        result must have length 2
      }

      new Scope {
        when(publisherDao.getPublishersForUser(isEq(Usercode("cusfal")))(any())).thenReturn(Seq(normalPublisher))
        when(publisherDao.getPublisherDepartments(isEq(normalPublisher.id))(any())).thenReturn(Seq("IN"))

        private val result = service.getPublishersForUser(Usercode("cusfal"))
        result must have length 1
        verify(publisherDao, never()).all(any())
      }
    }

    "allow users to view all publishers if they have global department permission" in {
      val globalPublisher = Publisher("123","Global Publisher")
      val normalPublisher = Publisher("234","Normal Publisher")
      val newsManagerPermission = PublisherPermission(Usercode("cusfal"), PublishingRole.NewsManager)

      new Scope {
        // Doesn't have any permission on the publisher
        when(publisherDao.getPublisherPermissions(isEq(normalPublisher.id), isEq(Usercode("cusfal")))(any())).thenReturn(Seq())
        when(publisherDao.getPublishersForUser(isEq(Usercode("cusfal")))(any())).thenReturn(Seq(globalPublisher))
        when(publisherDao.getPublisherDepartments(isEq(globalPublisher.id))(any())).thenReturn(Seq(PublisherService.AllDepartmentsWildcard))

        private val result = service.getRoleForUser(normalPublisher.id, Usercode("cusfal"))
        result.roles must have length 1
        result.roles.head mustBe Viewer
      }

      new Scope {
        // Has existing permission on the publisher
        when(publisherDao.getPublisherPermissions(isEq(normalPublisher.id), isEq(Usercode("cusfal")))(any())).thenReturn(Seq(newsManagerPermission))
        when(publisherDao.getPublishersForUser(isEq(Usercode("cusfal")))(any())).thenReturn(Seq(globalPublisher))
        when(publisherDao.getPublisherDepartments(isEq(globalPublisher.id))(any())).thenReturn(Seq(PublisherService.AllDepartmentsWildcard))

        private val result = service.getRoleForUser(normalPublisher.id, Usercode("cusfal"))
        result.roles must have length 2
      }
    }
  }

}
