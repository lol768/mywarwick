package services

import models.news.Audience
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import warwick.sso.{Group, GroupName, GroupService, Usercode}
import org.mockito.Matchers._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import scala.util.Success

class AudienceServiceTest extends PlaySpec with MockitoSugar {
  trait Ctx {
    val webgroups = mock[GroupService]
    val service = new AudienceServiceImpl(webgroups)

    def webgroupsIsEmpty: Unit = {
      when(webgroups.getWebGroup(any())).thenReturn(Success(None)) // this webgroups is empty
    }
  }

  import Audience._

  "AudienceService" should {

    "return an empty list for an empty audience" in new Ctx {
      service.resolve(Audience()).get must be (Nil)
    }

    "search for research postgrads" in new Ctx {
      webgroupsIsEmpty
      service.resolve(Audience(Seq(ResearchPostgrads)))
      verify(webgroups).getWebGroup(GroupName("all-postgraduate-research-ft"))
      verify(webgroups).getWebGroup(GroupName("all-postgraduate-research-pt"))
      verifyNoMoreInteractions(webgroups)
    }

    "search for taught postgrads" in new Ctx {
      webgroupsIsEmpty
      service.resolve(Audience(Seq(TaughtPostgrads)))
      verify(webgroups).getWebGroup(GroupName("all-postgraduate-taught-ft"))
      verify(webgroups).getWebGroup(GroupName("all-postgraduate-taught-pt"))
      verifyNoMoreInteractions(webgroups)
    }

  }
}
