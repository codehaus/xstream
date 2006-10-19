package org.codehaus.xstream.modeller.logic;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import de.java2html.Java2Html;
import freemarker.template.TemplateTransformModel;

public class Java2HtmlTransformer implements TemplateTransformModel {

	public Writer getWriter(Writer out, Map args) {
		return new Java2HtmlWriter(out);
	}

	private class Java2HtmlWriter extends Writer {

		private final Writer out;

		private final StringWriter buffer;

		Java2HtmlWriter(Writer out) {
			this.out = out;
			this.buffer = new StringWriter();
		}

		public void write(char[] cbuf, int off, int len) throws IOException {
			buffer.write(cbuf, off, len);
		}

		public void flush() throws IOException {
			out.write(Java2Html.convertToHtml(buffer.getBuffer().toString()));
			out.flush();
			buffer.getBuffer().delete(0, buffer.getBuffer().length());
		}

		public void close() {
			try {
				flush();
			} catch (IOException e) {
				// ignores the exception, nasty
			}
		}
	}
}
