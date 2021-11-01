package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.transform.XPathHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BrowsepathXPathBuilderTest {
    BrowsepathXPathBuilder parser;
    Logger LOGGER = Logger.getLogger("ParserTest.class");
    String nodesetInputFileName = "src/test/resources/ua/minimal-nodeset.xml";
    Document xmlDoc;


    @BeforeEach
    void setUp() throws TransformationException, IOException {
        try (InputStream nodesetStream = Files.newInputStream(Paths.get(nodesetInputFileName))) {
            UANodeSetTransformer transformer = new UANodeSetTransformer();
            xmlDoc = transformer.readXmlDocument(nodesetStream);

            parser = new BrowsepathXPathBuilder(xmlDoc);
        }
    }

    @Test
    void simpleTest() {
        String[] browsePath = {"1:ExampleObject", "1:ExampleIntegerVariable"};
        String nodeId = parser.getNodeIdFromBrowsePath(browsePath);
        assertEquals("ns=1;i=1010", nodeId);
        String exp = parser.pathExpression(browsePath);
        List<Node> nodeFromExp =XPathHelper.getInstance().getNodes(xmlDoc, exp);
        assertNotNull(nodeFromExp);
        assertEquals(nodeFromExp.size(), 1);
        assertThat(nodeFromExp.get(0)).isInstanceOf(Element.class);
        assertEquals(nodeId, ((Element)nodeFromExp.get(0)).attribute("NodeId").getValue() );
        assertEquals("http://iwu.fraunhofer.de/c32/arno", parser.getNamespace(browsePath[0]));


    }

}
