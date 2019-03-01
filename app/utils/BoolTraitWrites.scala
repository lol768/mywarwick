package utils

import play.api.libs.json._

import scala.reflect._

/**
  * Writes a JSON object of boolean values from a trait T.
  * Any non-boolean properties, and any non-accessor methods are ignored.
  */
class BoolTraitWrites[T : ClassTag] extends OWrites[T] {
  override def writes(o: T): JsObject = {
    // This would use Scala reflection instead of Java reflection if the former weren't so confusing
    val items = classTag[T].runtimeClass.getDeclaredMethods.filter { m =>
      m.getReturnType == classOf[Boolean] && m.getParameterCount == 0
    }.map { m =>
      m.getName -> JsBoolean(m.invoke(o).asInstanceOf[Boolean])
    }
    if (items.length == 0) {
      throw new IllegalArgumentException("No boolean accessors found")
    }
    JsObject(items.toSeq)
  }
}
