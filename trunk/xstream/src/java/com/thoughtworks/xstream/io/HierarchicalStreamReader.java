package com.thoughtworks.xstream.io;

import com.thoughtworks.xstream.converters.ErrorWriter;

/**
 * @author Joe Walnes
 */
public interface HierarchicalStreamReader {

    /**
     * Does the node have any more children remaining that have not yet been read?
     */
    boolean hasMoreChildren();

    void moveDown();

    void moveUp();

    /**
     * Get the name of the current node.
     */
    String getNodeName();

    /**
     * Get the value (text content) of the current node.
     */
    String getValue();

    /**
     * Get the value of an attribute of the current node.
     */
    String getAttribute(String name);

    /**
     * If any errors are detected, allow the reader to add any additional information that can aid debugging
     * (such as line numbers, XPath expressions, etc).
     */
    void appendErrors(ErrorWriter errorWriter);

    /**
     * Close the reader, if necessary.
     */
    void close();

    /**
     * Return the underlying HierarchicalStreamReader implementation.
     *
     * <p>If a Converter needs to access methods of a specific HierarchicalStreamReader implementation that are not
     * defined in the HierarchicalStreamReader interface, it should call this method before casting. This is because
     * the reader passed to the Converter is often wrapped/decorated by another implementation to provide additional
     * functionality (such as XPath tracking).</p>
     *
     * <p>For example:</p>
     * <pre>MySpecificReader mySpecificReader = (MySpecificReader)reader; <b>// INCORRECT!</b>
     * mySpecificReader.doSomethingSpecific();</pre>

     * <pre>MySpecificReader mySpecificReader = (MySpecificReader)reader.underlyingReader();  <b>// CORRECT!</b>
     * mySpecificReader.doSomethingSpecific();</pre>
     *
     * <p>Implementations of HierarchicalStreamReader should return 'this', unless they are a decorator, in which case
     * they should delegate to whatever they are wrapping.</p>
     */
    HierarchicalStreamReader underlyingReader();

    /**
     * @deprecated This method should not be used and is only provided for backwards compatability.
     *             As of XStream 1.1.1, you can use the {@link #underlyingReader()} method to get the underlying
     *             reader implementation and call implementation specific methods on that.
     */
    Object peekUnderlyingNode();

}
