package services

import org.mockito.ArgumentMatchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import services.dao.{NewsCategoryDao, UserNewsCategoryDao, UserPreferencesDao}
import warwick.sso.Usercode

class UserInitialisationServiceTest extends BaseSpec with MockitoSugar {

  val userPreferencesDao = mock[UserPreferencesDao]
  val newsCategoryDao = mock[NewsCategoryDao]
  val userNewsCategoryDao = mock[UserNewsCategoryDao]
  val service = new UserInitialisationServiceImpl(userPreferencesDao, newsCategoryDao, userNewsCategoryDao, new MockDatabase)

  "UserInitialisationService" should {

    val custard = Usercode("custard")

    "not initialise already-initialised user" in {
      when(userPreferencesDao.exists(isEq(custard))(any())).thenReturn(true)

      service.maybeInitialiseUser(custard)

      verify(userPreferencesDao, never).save(isEq(custard))(any())
    }

    "initialise users" in {
      when(newsCategoryDao.all()(any())).thenReturn(Seq.empty)

      when(userPreferencesDao.exists(isEq(custard))(any())).thenReturn(false)

      service.maybeInitialiseUser(custard)

      verify(userPreferencesDao).save(isEq(custard))(any())
    }

  }

}
