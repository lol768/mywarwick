package controllers.api

import helpers.{BaseSpec, MinimalAppPerSuite, OneStartAppPerSuite, WithActorSystem}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.analytics.{CombinedRow, NewsAnalyticsService}

class AnalyticsControllerTest extends BaseSpec with MockitoSugar with Results with MinimalAppPerSuite {

  "AnalyticsControllerTest" should {

    "newsItemReport should render Seq[CombinedRow] to a json object properly" in {

      val seqReturnedFromService = Seq[CombinedRow] (
        CombinedRow("04cb5644-e32c-46b2-920d-98811e48ecb8", 5,0),
        CombinedRow("183315a0-ff9a-42c2-a7ae-0f79b914c46f", 2,1)
      )

      val service = mock[NewsAnalyticsService]
      when(service.getClicks(any())).thenReturn(seqReturnedFromService)

      val expectingResponse = "{\"04cb5644-e32c-46b2-920d-98811e48ecb8\":{\"guests\":5,\"users\":0},\"183315a0-ff9a-42c2-a7ae-0f79b914c46f\":{\"guests\":2,\"users\":1}}"
      val requestToController = "{\"ids\":[\"87c8819b-96af-492a-9036-a8467c6b2be6\",\"183315a0-ff9a-42c2-a7ae-0f79b914c46f\",\"96fc8a45-3ef8-4c61-924f-4aeba961c650\",\"04cb5644-e32c-46b2-920d-98811e48ecb8\",\"5c983e2e-2275-4a31-b079-3743b1926283\",\"67e1bdc5-d5ce-4c25-9fa8-49289df9329d\",\"2a7028a1-54e9-451b-b403-92364fb29c9f\",\"1c618bc5-2574-4d83-a4f0-92d5166f4422\",\"1e635354-e488-42a7-bbfb-194fc6f2caef\",\"c7f7ed1c-e500-442a-a779-720592fb74bd\",\"c0661f9f-e9c9-4dc8-a29f-6f11d4ff25f5\",\"458622f1-20c0-446b-94e2-04e3399327f5\",\"3e305a36-0057-405a-92b2-018fc32943ff\",\"9ae7aacd-6b3d-4fa4-a49f-22f4839b62b0\",\"cb5a56f0-7dce-493a-b60b-4acb7916aa0d\",\"fada4180-f4f3-4aa0-ac91-7283de3a9646\",\"099e450f-4990-44e7-8653-ea2adb5eafe4\",\"1d86f180-a33f-4240-93f9-40f4724ca3bc\",\"b8d53da4-7ac1-4144-9ddd-d7df62fbbf69\",\"996e0129-3b46-419f-8708-9b49b4c44ddf\",\"48d9f888-d468-4dec-a790-6ef1c6d0824b\",\"9112d588-d3db-49aa-bb60-c0dbe1ab1963\",\"6f603490-c673-4de0-a7c6-6c3dc1a41689\",\"615b9584-0976-4bf9-b093-e7c15e51690f\",\"82edc5bb-bd82-4be3-ba56-a7050058112c\",\"fde119c2-8cac-43a4-9bb7-360390e62fa6\",\"2eae4699-6e32-4be1-a6a8-9b960133ecb7\",\"862d7223-2fe9-4a0f-b212-be787bc69483\",\"8fa8099a-5a85-4c23-8118-c8734d1881dd\",\"19b6ec2b-1b18-4d21-b28c-db6d6297c1f7\"]}"
      val controller = new AnalyticsController(service)
      controller.setControllerComponents(get[ControllerComponents])
      val request = FakeRequest("POST", "").withJsonBody(Json.parse(requestToController))

      val response = contentAsString(call(controller.newsItemReport(), request))

      response mustBe expectingResponse
    }

  }
}
