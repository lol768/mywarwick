import actors.UserActor
import akka.actor.Props
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {

    ClusterSharding(Akka.system).start(
      typeName = UserActor.typeName,
      entityProps = Props[UserActor],
      settings = ClusterShardingSettings(Akka.system),
      extractEntityId = UserActor.extractIdentityId,
      extractShardId = UserActor.extractShardId
    )

  }

}
