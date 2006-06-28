package com.thoughtworks.xstream.persistence;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A persistent map. Its values are actually serialized as xml files. If you
 * need an application-wide synchronized version of this map, try the respective
 * Collections methods.
 * 
 * @author Guilherme Silveira
 */
public class XmlMap extends AbstractMap {

	private final StreamStrategy streamStrategy;

	public XmlMap(StreamStrategy streamStrategy) {
		this.streamStrategy = streamStrategy;
	}

	public int size() {
		return streamStrategy.size();
	}


	public Object get(Object key) {
		// faster lookup
		return streamStrategy.get(key);
	}

	public Object put(Object key, Object value) {
		return streamStrategy.put(key,value);
	}

	public Object remove(Object key) {
		return streamStrategy.remove(key);
	}

	public Set entrySet() {
		return new XmlMapEntries();
	}

	class XmlMapEntries extends AbstractSet {

		public int size() {
			return XmlMap.this.size();
		}

		public boolean isEmpty() {
			return XmlMap.this.isEmpty();
		}

		public Iterator iterator() {
			return streamStrategy.iterator();
		}

	}


}