package com.sap.dsc.aas.lib.mapping;

public class TransformationContext {

	private Object forItem;

	public TransformationContext(Object forItem) {
		this.forItem = forItem;
	}

	public Object getForItem() {
		return forItem;
	}
	
	public static TransformationContext emptyContext() {
		return new TransformationContext(null);
	}

}
