package system

import actors.WorkerActor
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * The module is enabled in worker.conf.
  */
class WorkerModule extends AbstractModule with AkkaGuiceSupport {

  def configure = {
    // bindActor is eager so this creates the actor straight away
    bindActor[WorkerActor]("worker")
  }

}
