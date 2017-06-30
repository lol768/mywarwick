package views.utils
// From Tabula

import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import collection.JavaConverters._

import scala.collection.mutable

trait MarkdownRenderer {
	def renderMarkdown(source: String): String
}

trait MarkdownRendererImpl extends MarkdownRenderer {
	override def renderMarkdown(source: String): String = MarkdownRenderer.renderMarkdown(source)
}

object MarkdownRenderer {
	private val markdownParser = Parser.builder().extensions(mutable.Seq(AutolinkExtension.create()).asJava).build()
	private val markdownRenderer = HtmlRenderer.builder().build()

	def renderMarkdown(source: String): String =
		markdownRenderer.render(markdownParser.parse(source))
}
