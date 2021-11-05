package com.sap.dsc.aas.lib.mapping;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.sap.dsc.aas.lib.expressions.Expression;
import com.sap.dsc.aas.lib.mapping.model.Template;

public class TransformationContext {

	private Map<String, Expression> definitions = new HashMap<>();
	private Map<String, Expression> variables = new HashMap<>();
	private Object ctxItem;

	public TransformationContext(Object ctxItem) {
		this.ctxItem = ctxItem;
	}

	public TransformationContext(Object ctxItem, Template template) {
		this.ctxItem = ctxItem;
		if (template != null) {
			if (template.getDefinitions() != null) {
				definitions.putAll(template.getDefinitions());
			}
			if (template.getDefinitions() != null) {
				variables.putAll(template.getVariables());
			}
		}
	}

	public static TransformationContext emptyContext() {
		return new TransformationContext(null);
	}

	/**creates a new TransformationContext, inherits from an already existing parentCtx and add Template specific definitions
	 * 
	 * @param parentCtx TransformationContext to inherit Variables and Definitions from, might be null if nothing is to inherit from
	 * @param ctxItem 
	 * @param template Template which might contain new Variables and Definitions, might be null
	 * @return
	 */
	public static TransformationContext buildContext(TransformationContext parentCtx, Object ctxItem,
			Template template) {
		TransformationContext build = new TransformationContext(ctxItem);
		// take over parent ctx
		if (parentCtx != null) {
			if (parentCtx.getDefinitions() != null) {
				build.definitions.putAll(parentCtx.getDefinitions());
			}
			if (parentCtx.getVariables() != null) {
				build.variables.putAll(parentCtx.getVariables());
			}
		}
		// add and/or override with template context
		if (template != null) {
			if (template.getDefinitions() != null) {
				build.definitions.putAll(template.getDefinitions());
			}
			if (template.getVariables() != null) {
				build.variables.putAll(template.getVariables());
			}
		}
		return build;
	}

	public Optional<Object> getContextItem() {
		return Optional.fromNullable(ctxItem);
	}

	public Map<String, Expression> getDefinitions() {
		return definitions;
	}

	public Map<String, Expression> getVariables() {
		return variables;
	}

}
