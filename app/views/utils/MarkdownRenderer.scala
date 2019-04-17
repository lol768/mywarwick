package views.utils
// From Tabula

import com.vladsch.flexmark.ast.LinkNode
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.html.renderer.{AttributablePart, LinkResolverContext}
import com.vladsch.flexmark.html.{AttributeProvider, HtmlRenderer, IndependentAttributeProviderFactory}
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.html.Attributes
import com.vladsch.flexmark.util.options.MutableDataHolder
import play.twirl.api.Html

import scala.collection.JavaConverters._
import scala.collection.mutable

trait MarkdownRenderer {
	def renderMarkdown(source: String): String
}

trait MarkdownRendererImpl extends MarkdownRenderer {
	override def renderMarkdown(source: String): String = MarkdownRenderer.renderMarkdown(source)
}

object MarkdownRenderer {
  val specialCharacters: Set[Char] = Set('\n', '`', '>', '[', ']', '_', '*')

	private val markdownParser =
    Parser.builder()
      .extensions(mutable.Seq(
        AutolinkExtension.create()
      ).asJava)
      .build()

	private val markdownRenderer =
    HtmlRenderer.builder()
      .escapeHtml(true)
      .extensions(mutable.Seq(
        NewWindowLinksExtension()
      ).asJava)
      .build()

	def renderMarkdown(source: String): String =
		markdownRenderer.render(markdownParser.parse(source))
}

/**
  * Twirl helper to render HTML-escaped markdown
  */
object Markdown {
  /**
    * Creates an HTML fragment with the content rendered as Markdown.
    */
  def apply(text: String): Html = {
    Html(MarkdownRenderer.renderMarkdown(text))
  }
}

class NewWindowLinksExtension extends HtmlRenderer.HtmlRendererExtension {
  override def rendererOptions(options: MutableDataHolder): Unit = {}

  override def extend(rendererBuilder: HtmlRenderer.Builder, rendererType: String): Unit =
    rendererBuilder.attributeProviderFactory(new IndependentAttributeProviderFactory {
      override def create(context: LinkResolverContext): AttributeProvider =
        new NewWindowAttributeProvider
    })
}

object NewWindowLinksExtension {
  def apply(): NewWindowLinksExtension = new NewWindowLinksExtension
}

class NewWindowAttributeProvider extends AttributeProvider {
  override def setAttributes(node: Node, part: AttributablePart, attributes: Attributes): Unit =
    if (node.isInstanceOf[LinkNode] && part == AttributablePart.LINK)
      attributes.replaceValue("target", "_blank")
}
