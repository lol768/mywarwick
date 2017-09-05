package services.elasticSearch

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import models.Activity

@ImplementedBy(classOf[ActivityESClientImpl])
trait ActivityESClient {
  def index(activity: Activity)
}

@Singleton
class ActivityESClientImpl @Inject()(
  eSClientConfig: ESClientConfig
) extends ActivityESClient {

  private val client = eSClientConfig.getElastic4sTcpClient()

  override def index(activity: Activity): Unit = {
    ???
  }
}