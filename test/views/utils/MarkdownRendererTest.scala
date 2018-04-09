package views.utils

import helpers.BaseSpec

class MarkdownRendererTest extends BaseSpec {

  "MarkdownRenderer" should {
    "escape HTML included in input" in {
      val input = """Mixed Markdown with *emphasis* but also <img src="img.jpg"> HTML"""
      val expected = "<p>Mixed Markdown with <em>emphasis</em> but also &lt;img src=&quot;img.jpg&quot;&gt; HTML</p>\n"
      MarkdownRenderer.renderMarkdown(input) mustBe expected
    }

    "open links in a new window" in {
      val input =
        """To get the best out of [Link](http://a.com)
          |
          |http://warwick.ac.uk
          |
          |Love, _mum_
        """.stripMargin
      val expected =
        """<p>To get the best out of <a href="http://a.com" target="_blank">Link</a></p>
          |<p><a href="http://warwick.ac.uk" target="_blank">http://warwick.ac.uk</a></p>
          |<p>Love, <em>mum</em></p>
          |""".stripMargin
      MarkdownRenderer.renderMarkdown(input) mustBe expected
    }
  }

}
