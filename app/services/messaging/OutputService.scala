package services.messaging

import actors.MessageProcessing.ProcessingResult

import scala.concurrent.Future

trait OutputService {
  def send(message: MessageSend.Heavy): Future[ProcessingResult]
}
