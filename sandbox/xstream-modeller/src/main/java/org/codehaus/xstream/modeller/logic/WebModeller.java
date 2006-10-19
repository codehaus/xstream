package org.codehaus.xstream.modeller.logic;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.xstream.modeller.Graph;
import org.codehaus.xstream.modeller.XStreamModeller;
import org.vraptor.annotations.Component;
import org.vraptor.annotations.Out;
import org.vraptor.annotations.Parameter;
import org.xml.sax.SAXException;

@Component("webModeller")
public class WebModeller {

	private XStreamModeller modeller = new XStreamModeller(new Graph());

	private Graph graph;

	@Out
	// should be injected in the app scope instead of this out annotation
	private Java2HtmlTransformer html = new Java2HtmlTransformer();

	@Parameter
	private String xml;

	public void translate() throws ParserConfigurationException, SAXException,
			IOException {
		this.graph = modeller.parse(xml);
	}

	public String getXml() {
		return xml;
	}

	public Graph getGraph() {
		return graph;
	}

}
