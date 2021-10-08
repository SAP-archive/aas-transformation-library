package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.exceptions.TransformationException;
import com.sap.dsc.aas.lib.exceptions.UnableToReadXmlException;
import com.sap.dsc.aas.lib.transform.XPathHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
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

public class BrowsepathXPathBuilderTest {
    BrowsepathXPathBuilder parser;
    Logger LOGGER = Logger.getLogger("ParserTest.class");
    String nodesetInputFileName = "src/test/resources/ua/iwu.fraunhofer.joiningcell.xml";


    @BeforeEach
    void setUp() throws TransformationException, IOException {
        try (InputStream nodesetStream = Files.newInputStream(Paths.get(nodesetInputFileName))) {
            Document readXmlDocument = readXmlDocument(nodesetStream);
            XPathHelper.getInstance().setXmlRoot(readXmlDocument);
            parser = new BrowsepathXPathBuilder();
        }
    }

    @Test
    void simpleTest() throws Exception {
        List<Node> topologyElementType = XPathHelper.getInstance().xmlRoot.selectNodes("//*[@BrowseName=\"1:TopologyElementType\"]");
        LOGGER.info("topologyElementType: "+topologyElementType.get(0));
        List<Node> allChildren = XPathHelper.getInstance().xmlRoot.selectNodes("//*[@BrowseName=\"1:TopologyElementType\"]/*");
        LOGGER.info("allChildren: "+allChildren);
        List<Node> references = XPathHelper.getInstance().xmlRoot.selectNodes("//*[@BrowseName=\"1:TopologyElementType\"]/References");
        LOGGER.info("references: "+references);
//        List<Node> topologyElementType2 = XPathHelper.getInstance().getNodes("//*[@BrowseName=\"1:TopologyElementType\"]");
//       LOGGER.info("test5; "+ XPathHelper.getInstance().xmlRoot.getRootElement().selectNodes("/UANodeSet/*[@BrowseName=\"1:TopologyElementType\"]/References"));
//        List<Element> test2 = XPathHelper.getInstance().xmlRoot.getRootElement().elements().stream().filter(e ->
//                e.attributeValue("BrowseName") == "\"1:TopologyElementType\"").collect(Collectors.toList());
//        LOGGER.info("tets2: "+test2);
//
//        List<Element> test = XPathHelper.getInstance().xmlRoot.getRootElement().elements().stream().filter(e ->
//                e.attributeValue("BrowseName") == "\"1:TopologyElementType\"").filter(e2 ->
//                e2.element("References").elements("Reference").stream().anyMatch(e3 -> e3.getText() == "i=58" && e3.attributeValue("ReferenceType") == "\"HasSubtype\"" && e3.attributeValue("IsForward") == "\"false\"")
//        ).collect(Collectors.toList());
//        LOGGER.info("test: "+test);
//
//        String[] browsePath = {"1:TopologyElementType", "1:ParameterSet"};
//        String nodeId = parser.getNodeIdFromBrowsePath(browsePath);
//        LOGGER.info("-----------------------------------------------------------------------------");
//        assertEquals("ns=1;i=5002", parser.getNodeIdFromBrowsePath(browsePath));
//        LOGGER.info("-----------------------------------------------------------------------------");
//        String exp = parser.pathExpression(browsePath);
//        LOGGER.info("---------------------------------------------------------------------------- ");
//        List<Node> nodeIdFromExp =XPathHelper.getInstance().getNodes(exp);
//        assertNotNull(nodeIdFromExp);
//        assertEquals(nodeIdFromExp.size(), 1);
//        assertEquals(nodeId,nodeIdFromExp.get(0).getText() );
//        LOGGER.info("-----------------------------------------------------------------------------");
//        assertEquals("http://opcfoundation.org/UA/DI/", parser.getNamespace(browsePath[0]));


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
