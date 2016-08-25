package system

import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import org.quartz.Scheduler
import play.api.libs.concurrent.AkkaGuiceSupport
import services.healthcheck._
import services.messaging.{EmailOutputService, MobileOutputService, OutputService}
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

    // Enables Scheduler for injection. Scheduler.start() happens separately, in SchedulerConfigModule
    bind(classOf[Scheduler]).toProvider(classOf[SchedulerProvider])

    bindHealthChecks()
  }

  def bindHealthChecks(): Unit = {
    val multibinder = Multibinder.newSetBinder(binder(), new TypeLiteral[HealthCheck[_]] {})
    multibinder.addBinding().to(classOf[ClusterSizeHealthCheck])
    multibinder.addBinding().to(classOf[ClusterUnreachableHealthCheck])
    multibinder.addBinding().to(classOf[MessageQueueLengthHealthCheck])
    multibinder.addBinding().to(classOf[FailedMessageSendHealthCheck])
    multibinder.addBinding().to(classOf[MessageQueueOldestItemHealthCheck])
  }

  @Provides
  def currentApplication(trusted: TrustedApplicationsManager) = trusted.getCurrentApplication
}
