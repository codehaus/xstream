/*
 * Copyright (C) 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 13. July 2007 by Guilherme Silveira
 */
package com.thoughtworks.xstream.builder.processor;

import com.thoughtworks.xstream.XStream;

public class AliasTypeProcessor implements TypeConfigProcessor {

	private final String alias;

	public AliasTypeProcessor(String alias) {
		this.alias = alias;
	}

	public void process(XStream instance, Class type) {
		instance.alias(alias, type);
	}

}
