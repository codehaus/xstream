package com.thoughtworks.xstream.builder;

import com.thoughtworks.xstream.XStream;

/**
 * @author Guilherme Silveira
 */
interface XStreamBuilderConfigNode {
    void process(XStream instance);
}
