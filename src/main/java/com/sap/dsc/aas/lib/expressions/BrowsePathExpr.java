package com.sap.dsc.aas.lib.expressions;

import com.sap.dsc.aas.lib.mapping.TransformationContext;
import com.sap.dsc.aas.lib.ua.transform.BrowsepathXPathBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class BrowsePathExpr implements Expression {

    protected final List<Expression> args;

    public BrowsePathExpr( List<Expression> args){
        this.args = args;
    }

    @Override
    public String evaluate(TransformationContext ctx) {

        //FIXME Node context is not relevant to Browsepath functionality
//        if (!ctx.getContextItem().isPresent() ) {
//            throw new IllegalArgumentException("no Node Context is given.");
//        }
        List<String> path = args.stream().map(arg -> arg.evaluate(ctx)).filter(val -> val instanceof String).map(val -> (String) val).collect(Collectors.toList());
        String[] pathElems = new String[path.size()];
        String nodeId = BrowsepathXPathBuilder.getInstance().getNodeIdFromBrowsePath(path.toArray(pathElems));
        if(!path.isEmpty() && nodeId != null){
            return nodeId;
        }
        else{
            throw new IllegalArgumentException("@uaBrowsePath should be array of path elements as String, and should match exactly one UaNode.");
        }
    }
}
