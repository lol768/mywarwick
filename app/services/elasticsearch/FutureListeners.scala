package services.elasticsearch

import org.elasticsearch.action.ActionListener
import org.elasticsearch.client.{Response, ResponseListener}
import system.Logging

import scala.concurrent.Promise

class FutureResponseListener extends ResponseListener with Logging {

  private val responsePromise: Promise[Response] = Promise[Response]

  def onFailure(exception: Exception): Unit = {
    responsePromise.failure(exception)
    logger.error("Exception thrown after sending request to elasticsearch", exception)
  }

  override def onSuccess(response: Response): Unit = {
    responsePromise.success(response)
    logger.debug(s"Response received from elasticsearch: ${response.toString}")
  }

  def future = responsePromise.future

}


class FutureActionListener[R] extends ActionListener[R] {
  private val promise: Promise[R] = Promise[R]

  override def onFailure(e: Exception): Unit = promise.failure(e)

  override def onResponse(response: R): Unit = promise.success(response)

  def future = promise.future
}