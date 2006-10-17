package org.codehaus.xstream.modeller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.xstream.modeller.model.ModelGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XStreamModeller {

	private final Graph graph;

	public XStreamModeller(Graph graph) {
		this.graph = graph;
	}

	public static void main(String[] args) throws SAXException, IOException,
			ParserConfigurationException {

		// TODO read
		if (args.length != 1) {
			System.out
					.println("Invalid usage. Please give us the input xml file i.e.:");
			System.out.println("java -jar xstream-modeller.jar input.xml");
			return;
		}
		
		String filename = args[0];

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(filename));

		XStreamModeller easy = new XStreamModeller(new Graph());
		easy.parse(document.getChildNodes().item(0));

	}

	private void parse(Node doc) {

		DomNode n = new DomNode(doc);
		graph.find(n).loadFrom(n);

		ModelGenerator modelGen = new ModelGenerator();
		modelGen.printTypes(graph);

	}

	public Graph parse(String xml) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new ByteArrayInputStream(xml
				.getBytes()));
		parse(document.getChildNodes().item(0));
		return graph;
	}

}
