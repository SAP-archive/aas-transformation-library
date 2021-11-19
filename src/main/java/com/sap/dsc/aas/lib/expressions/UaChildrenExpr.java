package com.sap.dsc.aas.lib.expressions;

import com.sap.dsc.aas.lib.mapping.TransformationContext;
import com.sap.dsc.aas.lib.ua.transform.BrowsepathXPathBuilder;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.List;
import java.util.stream.Collectors;

public class UaChildrenExpr implements Expression {

    protected final List<Expression> args;

    public UaChildrenExpr( List<Expression> args){
        this.args = args;
    }

    @Override
    public List<Node> evaluate(TransformationContext ctx) {
        if(!(ctx.getContextItem() instanceof Node)) {
            throw new IllegalArgumentException("no Node Context is given.");
        }
        List<String> path = args.stream().map(arg -> arg.evaluate(ctx))
                .filter(val -> val instanceof String).map(val -> (String) val).collect(Collectors.toList());
        String[] pathElems = new String[path.size()];
        Node uaNode = BrowsepathXPathBuilder.getInstance().getNodeFromBrowsePath(path.toArray(pathElems));
        if(uaNode instanceof Element){
            return BrowsepathXPathBuilder.getInstance().getUaChildren((Element) uaNode);
        }
        else{
            throw new IllegalArgumentException("@uaBrowsePath should be array of path elements as String, and should match exactly one UaNode.");
        }
    }
}
