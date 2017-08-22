package helpers

import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import services.dao.AudienceLookupDao
import system.AppModule

class TestAppModule extends AppModule with AkkaGuiceSupport {
  override protected def bindAudienceLookupDao(): Unit = {
    bind(classOf[AudienceLookupDao])
      .annotatedWith(Names.named("tabula"))
      .to(classOf[MockAudienceLookupDao])
  }
}
