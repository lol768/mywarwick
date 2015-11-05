package controllers

import models.IncomingNotification
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import services.{NotificationCreationDao, NotificationDao, NotificationScopeDao, NotificationServiceImpl}

class NotificationServiceTest extends PlaySpec with MockitoSugar with Results {

  val notificationDao = mock[NotificationDao]
  val notificationScopeDao = mock[NotificationScopeDao]
  val notificationCreationDao = mock[NotificationCreationDao]
  val notificationService = new NotificationServiceImpl(notificationCreationDao, notificationDao, notificationScopeDao)

  "NotificationService" should {

    "save a notification" in {

      val notificationId = "123"
      when(notificationDao.save(any(), any())(any())).thenReturn(notificationId)
      when(notificationScopeDao.getNotificationsByScope(any(), any())).thenReturn(Seq())

      val createdNotificationId = notificationService.save(IncomingNotification("tabula", "coursework-due", "Coursework due", "Your submission for CH155 Huge Essay is due tomorrow", Map("module" -> "ch155"), any(), any()))

//      createdNotificationId mustBe notificationId

      verify(notificationScopeDao, times(1)).save(Matchers.eq(notificationId), Matchers.eq("module"), Matchers.eq("ch155"))

    }
//
//    "replaces notifications" in {
//
//      when(notificationScopeDao.getNotificationsByScope(Seq("ch155", "coursework-due"), "tabula")).thenReturn(Seq("456", "789"))
//
//      when(notificationDao.save(any(), any())).thenReturn("123")
//
//      notificationService.save(IncomingNotification("tabula", "coursework-due", "Coursework due", "Your submission for CH155 Huge Essay is due tomorrow", Seq("ch155", "coursework-due"), replace = true))
//
//      verify(notificationDao, times(1)).save(any(), Matchers.eq(Seq("456", "789")))
//
//    }

  }

}
