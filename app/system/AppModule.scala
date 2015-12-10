package system

import com.google.inject.{Provides, AbstractModule}
import uk.ac.warwick.sso.client.trusted.TrustedApplicationsManager


class AppModule extends AbstractModule {
  override def configure(): Unit = {

  }

  @Provides
  def currentApplication(trusted: TrustedApplicationsManager) = trusted.getCurrentApplication
}
