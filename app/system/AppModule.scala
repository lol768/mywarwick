package system

import actors.{MessageBus, WorkerActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 *
 */
class AppModule extends AbstractModule with AkkaGuiceSupport {

  def configure = {
    bind(classOf[MessageBus])
  }

}
