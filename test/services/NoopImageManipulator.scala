package services

import java.awt.image.BufferedImage

class NoopImageManipulator extends ImageManipulator {

  def resizeToWidth(image: BufferedImage, width: Int) = image

}
