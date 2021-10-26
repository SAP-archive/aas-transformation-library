package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.transform.XPathHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
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
    String nodesetInputFileName = "src/test/resources/ua/iwu.fraunhofer.joiningcell.xml";


    @BeforeEach
    void setUp() throws TransformationException, IOException {
        try (InputStream nodesetStream = Files.newInputStream(Paths.get(nodesetInputFileName))) {
            Document readXmlDocument = readXmlDocument(nodesetStream);
            XPathHelper.getInstance().setXmlRoot(readXmlDocument);
            XPathHelper.getInstance().setNamespaceBinding("uax", "http://opcfoundation.org/UA/2008/02/Types.xsd" );
            XPathHelper.getInstance().setNamespaceBinding("si", "http://www.siemens.com/OPCUA/2017/SimaticNodeSetExtensions" );

            parser = new BrowsepathXPathBuilder();
        }
    }

    @Test
    void simpleTest() {
        String[] browsePath = {"1:TopologyElementType", "1:ParameterSet"};
        String nodeId = parser.getNodeIdFromBrowsePath(browsePath);
        assertEquals("ns=1;i=5002", nodeId);
        String exp = parser.pathExpression(browsePath);
        List<Node> nodeFromExp =XPathHelper.getInstance().getNodes(exp);
        assertNotNull(nodeFromExp);
        assertEquals(nodeFromExp.size(), 1);
        assertThat(nodeFromExp.get(0)).isInstanceOf(Element.class);
        assertEquals(nodeId, ((Element)nodeFromExp.get(0)).attribute("NodeId").getValue() );
        assertEquals("http://opcfoundation.org/UA/DI/", parser.getNamespace(browsePath[0]));


    }

    public Document readXmlDocument(InputStream stream) throws TransformationException {
        try {
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return reader.read(stream);
        } catch (DocumentException | SAXException e) {
            throw new UnableToReadXmlException("Unable to load UA NodeSet structure", e);
        }
    }
}
