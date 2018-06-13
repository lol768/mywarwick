package services.helper

import helpers.BaseSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.ahc.{AhcWSRequest, StandaloneAhcWSClient, StandaloneAhcWSRequest}

class WSRequestUriBuilderTest extends BaseSpec with MockitoSugar {

  private val client = mock[StandaloneAhcWSClient]

  "WSRequestUriBuilder" should {

    "encode parameters as expected" in {
      val underlying = StandaloneAhcWSRequest(
        client = client,
        url = "https://tabula-test.warwick.ac.uk/api/v1/universityIdSearch",
        queryString = Map("hallsOfResidence" -> Seq("Arthur Vick"))
      )(null)
      val request = AhcWSRequest(underlying)
      val uri = WSRequestUriBuilder.buildUri(request)
      uri.toString must be ("https://tabula-test.warwick.ac.uk/api/v1/universityIdSearch?hallsOfResidence=Arthur%20Vick")
      uri.getQuery must be ("hallsOfResidence=Arthur%20Vick")
    }

  }

}