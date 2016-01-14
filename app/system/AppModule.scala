package system

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provides}
import play.api.libs.concurrent.AkkaGuiceSupport
import services.messaging.{MobileOutputService, APNSOutputService, EmailOutputService, OutputService}
import uk.ac.warwick.sso.client.trusted.TrustedApplicationsManager


class AppModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[OutputService])
      .annotatedWith(Names.named("email"))
      .to(classOf[EmailOutputService])

    bind(classOf[OutputService])
      .annotatedWith(Names.named("mobile"))
      .to(classOf[MobileOutputService])

    // Start this up straight away so we always manage the cluster.
    bind(classOf[ClusterLifecycle]).asEagerSingleton()
  }

  @Provides
  def currentApplication(trusted: TrustedApplicationsManager) = trusted.getCurrentApplication
}
