package system

import actors.SystemRoot
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AkkaModule extends AbstractModule with AkkaGuiceSupport {

  def configure = {
    bindActor[SystemRoot]("system-root-actor")
  }

}
