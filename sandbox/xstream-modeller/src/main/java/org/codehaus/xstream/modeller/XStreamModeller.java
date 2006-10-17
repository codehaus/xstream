package org.codehaus.xstream.modeller;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.xstream.modeller.config.ConfigGenerator;
import org.codehaus.xstream.modeller.config.XStreamConfigGenerator;
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
		if(args.length!=2) {
			System.out.println("Invalid usage. Please give us the input xml file and the desired package i.e.:");
			System.out.println("java -jar xstream-modeller.jar input.xml org.codehaus.xstream");
			return;
		}
		String filename = args[0];
		String basePackage = args[1];

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(filename));

		XStreamModeller easy = new XStreamModeller(new Graph());
		easy.parse(document.getChildNodes().item(0), basePackage);

	}

	private void parse(Node doc, String basePackage) {
		
		DomNode n = new DomNode(doc);
		graph.find(n).loadFrom(n);
		
		ModelGenerator modelGen = new ModelGenerator(basePackage);
		modelGen.printTypes(graph);

		ConfigGenerator configGen = new XStreamConfigGenerator(basePackage);
		configGen.printConfiguration(graph);
		
	}

}
