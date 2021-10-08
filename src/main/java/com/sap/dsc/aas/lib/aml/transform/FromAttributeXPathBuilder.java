package com.sap.dsc.aas.lib.aml.transform;

import com.sap.dsc.aas.lib.transform.XPathBuilder;

public class FromAttributeXPathBuilder implements XPathBuilder {

    @Override
    public String pathExpression(String[] input) {
        String xpath = null;
        if(input != null && input.length ==1){
            xpath = "caex:Attribute[@Name='" + input[0] + "']";//FIXME
        }
        return xpath;
    }
}
