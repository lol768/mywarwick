package system

import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import org.quartz.Scheduler
import play.api.libs.concurrent.AkkaGuiceSupport
import services.FeaturesService
import services.Features
import services.elasticsearch.{ActivityESService, ActivityESServiceImpl}
import services.dao.{AudienceLookupDao, TabulaAudienceLookupDao}
import services.healthcheck._
import services.messaging.{EmailOutputService, MobileOutputService, OutputService, SmsOutputService}
import uk.ac.warwick.sso.client.trusted.TrustedApplicationsManager

class AppModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[OutputService])
      .annotatedWith(Names.named("email"))
      .to(classOf[EmailOutputService])

    bind(classOf[OutputService])
      .annotatedWith(Names.named("mobile"))
      .to(classOf[MobileOutputService])

    bind(classOf[OutputService])
      .annotatedWith(Names.named("sms"))
      .to(classOf[SmsOutputService])

    bindAudienceLookupDao()

    // Start this up straight away so we always manage the cluster.
    bind(classOf[ClusterLifecycle]).asEagerSingleton()

    // Enables Scheduler for injection. Scheduler.start() happens separately, in SchedulerConfigModule
    bind(classOf[Scheduler]).toProvider(classOf[SchedulerProvider])

    // eagerly bind ActivityESService to ensure activity's template is set at first
    bind(classOf[ActivityESService]).to(classOf[ActivityESServiceImpl]).asEagerSingleton()

    bind(classOf[Features]).toProvider(classOf[FeaturesService])

    bindHealthChecks()
  }

  def bindHealthChecks(): Unit = {
    val multibinder = Multibinder.newSetBinder(binder(), new TypeLiteral[HealthCheck[_]] {})
    multibinder.addBinding().to(classOf[ClusterSizeHealthCheck])
    multibinder.addBinding().to(classOf[ClusterUnreachableHealthCheck])
    multibinder.addBinding().to(classOf[MessageQueueLengthHealthCheck])
    multibinder.addBinding().to(classOf[FailedMessageSendHealthCheck])
    multibinder.addBinding().to(classOf[MessageQueueOldestItemHealthCheck])
    multibinder.addBinding().to(classOf[PublisherAlertFrequencyHealthCheck])
    multibinder.addBinding().to(classOf[SmsSentLast24HoursHealthCheck])
  }

  protected def bindAudienceLookupDao(): Unit = {
    bind(classOf[AudienceLookupDao])
      .annotatedWith(Names.named("tabula"))
      .to(classOf[TabulaAudienceLookupDao])
  }

  @Provides
  def currentApplication(trusted: TrustedApplicationsManager) = trusted.getCurrentApplication
}
