package services

import java.awt.image.BufferedImage

import com.google.inject.{ImplementedBy, Singleton}
import org.imgscalr.Scalr
import org.imgscalr.Scalr.Mode._

@ImplementedBy(classOf[ScalrImageManipulator])
trait ImageManipulator {

  def resizeToWidth(image: BufferedImage, width: Int): BufferedImage

}

@Singleton
class ScalrImageManipulator extends ImageManipulator {

  def resizeToWidth(image: BufferedImage, width: Int): BufferedImage =
    Scalr.resize(image, FIT_TO_WIDTH, width, 0)

}
