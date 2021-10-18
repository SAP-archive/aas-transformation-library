package com.sap.dsc.aas.lib;

import com.sap.dsc.aas.lib.transform.XPathHelper;

public class TestUtils {

	/**
	 * should reset all singletons to avoid state to creep into each others unit
	 * tests
	 * 
	 * see
	 * https://stackoverflow.com/questions/54035180/reset-singleton-for-each-unit-test-java
	 * 
	 * @throws Exception
	 */
	public static void resetBindings() throws Exception {
		java.lang.reflect.Field instance = XPathHelper.class.getDeclaredField("instance");
		instance.setAccessible(true);
		instance.set(null, null);
	}
	
	public static void setAMLBindings() throws Exception {
    	XPathHelper.getInstance().setNamespaceBinding("caex", "http://www.dke.de/CAEX");
	}


}
