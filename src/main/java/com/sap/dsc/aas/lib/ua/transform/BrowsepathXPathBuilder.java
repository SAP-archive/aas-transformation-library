package com.sap.dsc.aas.lib.ua.transform;

import com.sap.dsc.aas.lib.transform.XPathBuilder;
import com.sap.dsc.aas.lib.transform.XPathHelper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

public class BrowsepathXPathBuilder implements XPathBuilder {

    private final XPathHelper xpathHelper;
    private final List<String> namespaceURIs;
    private final Set<String> hierarchyReferences;
    private String hierarchyIsConstraint ;
    private Document root;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public BrowsepathXPathBuilder(Document root){
        this.root = root;
        String DEFAULT_NS = "http://opcfoundation.org/UA/";
        xpathHelper = XPathHelper.getInstance();
        hierarchyReferences = new HashSet<>();
        hierarchyReferences.addAll(Arrays.asList(Hierarchy.HAS_COMPONENT.nodeId, Hierarchy.HAS_PROPERTY.nodeId, Hierarchy.ORGANIZES.nodeId));
        namespaceURIs = new ArrayList<>();
        namespaceURIs.add(DEFAULT_NS);
        namespaceURIs.addAll(root.getRootElement().element("NamespaceUris").elements("Uri")
                .stream().map(Element::getText).collect(Collectors.toList()));
        hierarchyReferences.addAll(root.getRootElement().element("Aliases").elements("Alias")
                .stream().filter(e -> Hierarchy.hierarchyReferences().contains(e.getText()))
                         .map(e -> e.attributeValue("Alias"))
                         .collect(Collectors.toSet()));
        hierarchyIsConstraint="(";
        for (String ref : hierarchyReferences) {
            hierarchyIsConstraint += "@ReferenceType=\"" + ref + "\" or ";
        }
        hierarchyIsConstraint = hierarchyIsConstraint.substring(0, hierarchyIsConstraint.length() - 3) + ") and @IsForward= \"false\"";
    }

    @Override
    public String pathExpression(String[] browsePath) {
        if (browsePath == null || browsePath.length == 0 || browsePath[0] == null || browsePath[0].replace("/", "").trim().isEmpty()) {
            return null;
        }
        String exp = "/UANodeSet/*[@BrowseName=\"" + browsePath[0] + "\"]/@NodeId";
        if (browsePath.length == 1) {
            return exp;
        }
        List<Node> parentIDs = xpathHelper.getNodes(root, exp);
        if (parentIDs == null || parentIDs.isEmpty()) {
            return null;
        }
        return getExpForBrowsePath(Arrays.copyOfRange(browsePath, 1, browsePath.length), parentIDs.get(0).getText());
    }

    private String getExpForBrowsePath(String[] browsePath, String parentID) {
        if (browsePath == null || browsePath.length == 0 || browsePath[0].isEmpty() || browsePath[0].replace("/", "").trim().isEmpty()) {
            return parentID;
        }
        String exp = computeExpression( browsePath[0], parentID);
        if (browsePath.length == 1) {
            return exp;
        }
       List<Node> parents = xpathHelper.getNodes(root, exp);
        if (parents == null || parents.isEmpty()) {
            return null;
        }
        String[] newPath = Arrays.copyOfRange(browsePath, 1, browsePath.length);
        return getNodeIdFromBrowsePath(newPath, parents.get(0));

    }

    public String getNodeIdFromBrowsePath(String[] browsePath) {
        return getNodeIdFromBrowsePath(browsePath, null);
    }

    public String getNodeIdFromBrowsePath(String[] browsePath, Node prev) {
        if (browsePath == null || browsePath.length == 0 || browsePath[0] == null || browsePath[0].replace("/", "").trim().isEmpty()) {
            return prev == null ? null : prev.getText();
        }
        List<Node> parents = xpathHelper.getNodes(root,"/UANodeSet/*[@BrowseName=\"" + browsePath[0] + "\"]");
        if (parents == null || parents.size() == 0 || parents.get(0) == null) {
            return prev == null ? null : prev.getText();
        }
        //start node in browse path (when prev == null) should be unique in NodeSet
        // if prev is null then we are on start node in browse path therefore parents should have only one parent, else chose the parent that it's parent is prev
        Node parent = null;
        if (prev == null) {
            if (parents.size() != 1)
                return null;
            parent = parents.get(0);
        } else {
            for (int p = 1; p < parents.size(); p++) {
                if (isParent((Element)prev, (Element)parents.get(p))) {
                    parent = parents.get((p));
                    break;
                }
            }
        }
        if (parent == null || (parent.getNodeType() == Node.ELEMENT_NODE && ((Element) parent).attributeCount() == 0 )) {
            return prev == null ? null : prev.getText();
        }
        Element parentElem = (Element) parent;
        String parentId =  parentElem.attributeValue("NodeId");
        if (browsePath.length == 1 &&  parentElem.attributeCount() > 0)
            return parentId;
        //considering references: "HasComponent", "HasProperty" and "Organizes" to determine children of a node
        // Child and Parent relationships are in context of these references
        List<Node> childrenNodeId = xpathHelper.getNodes(root,"/UANodeSet/*[@BrowseName=\"" + browsePath[1] + "\"]/@NodeId");
        Node childNode = findReferencedChild(parent, childrenNodeId);
        if (childNode != null) {
            return getNodeIdFromBrowsePath(Arrays.copyOfRange(browsePath, 2, browsePath.length), childNode);
        }

        //considering references: "IsComponent", "IsProperty" and "OrganizedBy" to determine children of a node
        String exp = computeExpression(browsePath[1],parentId);
        List<Node> nodes = xpathHelper.getNodes(root,exp);//xpathHelper.xmlRoot.elements(exp);//
        if (nodes == null || nodes.size() == 0 || nodes.get(0).getNodeType() != Node.ELEMENT_NODE) {
            return prev == null ? null : prev.getText();
        }
        String[] newPath = Arrays.copyOfRange(browsePath, 2, browsePath.length);
        Node nodeId = ((Element)nodes.get(0)).attribute("NodeId");
        return getNodeIdFromBrowsePath(newPath, nodeId);
    }

    /*
    parent and child relationships don't have the meaning of xml nodes relationships
    theses relationships are in context of UANode set (in context of "HasComponent","HasProperty", "Organizes"
     */
    private boolean isParent(Element parent, Element child) {
        if (parent == null || child == null ||
                child.getNodeType() != Node.ELEMENT_NODE || child.attributeCount() ==0 ||
                parent.getNodeType() != Node.ELEMENT_NODE || parent.attributeCount() ==0){
            return false;
        }
        return isParent(parent, child, true) || isParent(child, parent, false);
    }

    private boolean isParent(Element parent, Element child, boolean isForward) {
        Element reference = parent.element("References");
        if (reference == null)
            return false;
        List<Element> refs = reference.elements("Reference");
        if (refs == null || refs.isEmpty())
            return false;
        for(Element ref: refs) {
            String isChild = isForward ? "true" : "false";
            String refType = ref.attributeValue("ReferenceType");
            String childId =  child.attributeValue("NodeId");
            if(Objects.equals(ref.attributeValue("IsForward"), isChild) &&
                    hierarchyReferences.contains(refType) && ref.getText().equals(childId)){
                return true;
            }
        }
        return false;
    }

    private Node findReferencedChild(Node parent, List<Node> childrenNodes) {
        if (parent == null || childrenNodes == null)
            return null;
        Element references = ((Element) parent).element("References");
        if (references == null )
            return null;
       List<Element> refs = references.elements("Reference");
        if(refs == null || refs.isEmpty())
                return null;
        for(Element ref:refs) {
            String refType = ref.getText();
            String isForward =  ref.attributeValue("isForward");
            if (hierarchyReferences.contains(refType) && isForward != null && Objects.equals(isForward, "true") &&
                    getNodeIdSet(childrenNodes).contains(refType))
                return ref;
        }
        return null;
    }

    private Set<String> getNodeIdSet(List<Node> childrenNodes) {
        if (childrenNodes == null || childrenNodes.size() == 0)
            return new TreeSet<>();
        return childrenNodes.stream().filter(ch -> ch instanceof  Element).map(ch -> ((Element) ch).attributeValue("NodeId")).collect(Collectors.toSet());
    }

    public String getNamespace(String browseName) {
        if (browseName == null || browseName.trim().isEmpty()) {
            return namespaceURIs.get(0);
        }
        String[] bn = browseName.split(":");
        if (bn.length == 1) {
            return namespaceURIs.get(0);
        } else if (bn.length > 1) {
            try {
                int index = Integer.parseInt(bn[0]);
                if (index >= 0 && index < namespaceURIs.size())
                    return namespaceURIs.get(index);
            } catch (NumberFormatException e) {
                return namespaceURIs.get(0);
            }
        }
        return null;
    }

    private String computeExpression(String browseName, String text){
       return  "/UANodeSet/*[@BrowseName=\"" + browseName + "\"]/*[name()=\"References\"]/*[name()=\"Reference\" and " +
                "text()= \"" + text + "\" and (" +
                hierarchyIsConstraint +
                ")]/parent::*/parent::*";
    }

    enum Hierarchy {
        HAS_COMPONENT("i=47"), HAS_PROPERTY("i=46"), ORGANIZES("i=35");
        String nodeId;

        Hierarchy(String nodeId) {
            this.nodeId = nodeId;
        }
        public static Set<String> hierarchyReferences(){
            return new HashSet<>(Arrays.asList(HAS_COMPONENT.nodeId, HAS_PROPERTY.nodeId, ORGANIZES.nodeId));
        }
    }

}
