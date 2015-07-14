package services

import com.google.inject.ImplementedBy

import scala.concurrent.Future
import scala.util.Random
import PrinterService._

object PrinterService {
  case class Response(val username: String, val balance: Int)
}

@ImplementedBy(classOf[PrinterServiceImpl])
trait PrinterService {
  def get(username: String): Future[Response]
}

class PrinterServiceImpl extends PrinterService {
  def get(username: String) = Future.successful( Response(username, Random.nextInt(100)) )
}

