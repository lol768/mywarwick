package services.dao

import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.db.Database

class ProviderPermissionDaoTest extends PlaySpec with OneStartAppPerSuite {

  val providerPermissionDao = app.injector.instanceOf[ProviderPermissionDao]

  "ProviderPermissionDao" should {

    "allow posting" in {
      providerPermissionDao.allow("tabula", "someone")
      providerPermissionDao.canUserPostForProvider("tabula", "someone") mustBe true
    }

    "disallow posting" in {
      providerPermissionDao.disallow("tabula", "someone")
      providerPermissionDao.canUserPostForProvider("tabula", "someone") mustBe false
    }

    "not break if adding the same permission twice" in {
      providerPermissionDao.allow("tabula", "someone")
      providerPermissionDao.allow("tabula", "someone")
    }

    "not break if removing the same permission twice" in {
      providerPermissionDao.disallow("tabula", "someone")
      providerPermissionDao.disallow("tabula", "someone")
    }

  }

}
