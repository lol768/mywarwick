package system

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.{Charset, CodingErrorAction, StandardCharsets}

object StringUtils {

  /**
    * Truncate a string so that it will fit into a specific number of bytes.
    * It's not super efficient but it has some optimisations for common cases.
    * In the worst case it will encode to UTF-8 then decode part of it back to
    * a String, but in many cases it will avoid doing all that.
    */
  def truncateToBytes(maxBytes: Int, text: String, encoding: Charset = StandardCharsets.UTF_8): String = {
    // optimization for short strings,
    // assuming text will be less than 2b/char on average.
    // this is the only bit that could technically return invalid data.
    if (text.length <= maxBytes / 2) {
      text
    } else {
      val bytes = text.getBytes(encoding)
      if (bytes.length <= maxBytes) {
        // It fits, great
        text
      } else if (bytes.length == text.length) {
        // It's all ANSI, so bytes==chars
        truncateToChars(maxBytes, text)
      } else {
        // Decode $maxBytes bytes until we run out.
        val decoder = encoding.newDecoder
        // We might end on half a char; just drop it
        decoder.onMalformedInput(CodingErrorAction.IGNORE)
        decoder.reset()
        decoder.decode(ByteBuffer.wrap(bytes, 0, maxBytes)).toString
      }
    }
  }

  private def truncateToChars(maxChars: Int, text: String): String =
    if (text.length > maxChars) text.substring(0, maxChars)
    else text
}

