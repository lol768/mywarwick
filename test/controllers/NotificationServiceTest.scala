package controllers

import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import services.{NotificationDao, NotificationScopeDao, NotificationServiceImpl}

class NotificationServiceTest extends PlaySpec with MockitoSugar with Results {

  val notificationDao = mock[NotificationDao]
  val notificationScopeDao = mock[NotificationScopeDao]
  val notificationService = new NotificationServiceImpl(notificationDao, notificationScopeDao)

  "NotificationService" should {

    "save a notification" in {

      val notificationId = "123"
      when(notificationDao.save(any(), any(), any(), any(), any())).thenReturn(notificationId)

      val createdNotificationId = notificationService.save("tabula", "coursework-due", "Coursework due", "Your submission for CH155 Huge Essay is due tomorrow", Seq("ch155", "coursework-due"), replace = false)

      createdNotificationId mustBe notificationId

      verify(notificationScopeDao, times(2)).save(Matchers.eq(notificationId), any())

    }

    "replaces notifications" in {

      when(notificationScopeDao.getNotificationsByScope(Seq("ch155", "coursework-due"), "tabula")).thenReturn(Seq("456", "789"))

      when(notificationDao.save(any(), any(), any(), any(), any())).thenReturn("123")

      notificationService.save("tabula", "coursework-due", "Coursework due", "Your submission for CH155 Huge Essay is due tomorrow", Seq("ch155", "coursework-due"), replace = true)

      verify(notificationDao, times(1)).save(any(), any(), any(), any(), Matchers.eq(Seq("456", "789")))

    }

  }

}
