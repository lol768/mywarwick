import actors.UsersActor
import akka.actor.Props
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {

    Akka.system.actorOf(Props[UsersActor], "users")

  }

}
