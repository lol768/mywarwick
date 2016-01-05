package system

import com.google.inject.AbstractModule
import org.quartz.Scheduler

class SchedulerModule extends AbstractModule {

  def configure = {
    bind(classOf[Scheduler]).toProvider(classOf[SchedulerProvider]).asEagerSingleton()
  }

}
