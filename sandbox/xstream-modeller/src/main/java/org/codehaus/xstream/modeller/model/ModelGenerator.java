package org.codehaus.xstream.modeller.model;

import java.util.Collection;

import org.codehaus.xstream.modeller.Graph;
import org.codehaus.xstream.modeller.XNode;
import org.codehaus.xstream.modeller.dom.Element;

public class ModelGenerator {

	public void printTypes(Graph graph) {

		Collection<XNode> nodes = graph.getElements();
		for (XNode node : nodes) {
			Element type = (Element) node.getType();
			if (type.isImplicitCollection()) {
				continue;
			}
			System.out.println(type.getCode());
			System.out.println();
		}

	}

}
