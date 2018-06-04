package system

import akka.actor.ActorSystem
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton

import scala.concurrent.ExecutionContext

class ThreadsModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  @Named("tileData")
  def tileData(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.tileData")

  @Provides
  @Singleton
  @Named("email")
  def email(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.email")

  @Provides
  @Singleton
  @Named("mobile")
  def mobile(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.mobile")

  @Provides
  @Singleton
  @Named("sms")
  def sms(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.sms")

  @Provides
  @Singleton
  @Named("externalData")
  def externalData(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.externalData")

  @Provides
  @Singleton
  @Named("elastic")
  def elastic(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.elastic")

  @Provides
  @Singleton
  @Named("web")
  def web(akka: ActorSystem): ExecutionContext =
    akka.dispatchers.lookup("threads.web")

}
