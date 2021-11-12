package com.sap.dsc.aas.lib.mapping;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sap.dsc.aas.lib.expressions.Expression;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.mapping.model.Template;

import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;

public class AASMappingTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AssetAdministrationShellEnvironment transform(MappingSpecification mappingSpec, Object initialContextItem) {
		TransformationContext initialCtx = TransformationContext.buildContext(null, initialContextItem, null);
		AssetAdministrationShellEnvironment aasEnvTemplate = mappingSpec.getAasEnvironmentMapping();
		if (mappingSpec.getAasEnvironmentMapping() instanceof Template
				&& ((Template) mappingSpec.getAasEnvironmentMapping()).getForeachExpression() != null) {
			LOGGER.warn(
					"@forEach expression on top level AAS Environment is not applicable. Only one AAS Environments will be returned!");
		}
		return (AssetAdministrationShellEnvironment) asList(transformAny(aasEnvTemplate, initialCtx)).get(0);
	}

	private List<? extends Object> inflateTemplate(Template template, TransformationContext parentCtx) {
		List<Object> inflated = new ArrayList<>();
		Expression foreachExpression = template.getForeachExpression();
		if (foreachExpression != null) {
			Object evaluate = template.getForeachExpression().evaluate(parentCtx);
			List<Object> forItems = asList(evaluate);
			for (Object forItem : forItems) {
				TransformationContext childCtx = TransformationContext.buildContext(parentCtx, forItem, template);
				inflated.add(transformSingle(template, childCtx));
			}
		} else {
			TransformationContext childCtx = TransformationContext.buildContext(parentCtx, parentCtx.getContextItem(),
					template);
			inflated.add(transformSingle(template, childCtx));
		}
		return inflated;
	}

	private Object transformSingle(Template template, TransformationContext ctx) {
		Class<?> aasInterface = getAASInterface(template);
		Object transformedEntity = null;
		if (template.getBindSpecification() != null) {
			transformedEntity = createInstanceByBindings(template, aasInterface, ctx);
		} else {
			transformedEntity = newDefaultAASInstance(aasInterface);
		}
		try {
			transformProperties(template, ctx, transformedEntity);
		} catch (NoSuchMethodException | SecurityException | IntrospectionException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error("Failed to transform properties for " + transformedEntity.getClass().getName(), e);
		}
		return transformedEntity;
	}

	private Object createInstanceByBindings(Template template, Class<?> aasInterface, TransformationContext ctx) {
		Object transformedEntity;
		Map<String, String> evaluatedBindings = new HashMap<>();
		Set<Entry<String, Expression>> bindings = template.getBindSpecification().getBindings().entrySet();
		for (Entry<String, Expression> binding : bindings) {
			String evaluate = binding.getValue().evaluateAsString(ctx);
			evaluatedBindings.put(binding.getKey(), evaluate);
		}
		JsonDeserializer jsonDeserializer = new JsonDeserializer();
		Field mapperField;
		try {
			// to discuss: get the mapper as property
			mapperField = JsonDeserializer.class.getDeclaredField("mapper");
			mapperField.setAccessible(true);
			JsonMapper jsonMapper = (JsonMapper) mapperField.get(jsonDeserializer);
			transformedEntity = jsonMapper.convertValue(evaluatedBindings, aasInterface);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LOGGER.error("Failed to read binding specification, evaluated as '{}' for model '{}'. Error: '{}'",
					evaluatedBindings, aasInterface.getName(), e.getMessage());
			transformedEntity = newDefaultAASInstance(aasInterface);
		}
		return transformedEntity;
	}

	private void transformProperties(Template template, TransformationContext ctx, Object transformationTarget)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(transformationTarget.getClass())
				.getPropertyDescriptors();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			Method readMethod = propertyDescriptor.getReadMethod();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (readMethod != null && writeMethod != null) {
				Object templateReadProperty = template.getClass().getMethod(readMethod.getName()).invoke(template);
				Object transformedTemplateReadProperty = transformAny(templateReadProperty, ctx);

				if (transformedTemplateReadProperty instanceof Iterable) {
					List<Object> transformedProperties = asList(transformedTemplateReadProperty);
					if (Iterable.class.isAssignableFrom(writeMethod.getParameterTypes()[0])) {
						writeMethod.invoke(transformationTarget, transformedProperties);
					} else if (transformedProperties.isEmpty()) {
						// do nothing
					} else if (transformedProperties.size() == 1) {
						writeMethod.invoke(transformationTarget, transformedProperties.get(0));
					} else {
						LOGGER.warn(
								"The result of a property transformation is a list with multiple entries, but the target ({}#{}) is not a list. First item of list '{}' will be used.",
								transformationTarget.getClass().getName(), writeMethod.getName(),
								transformedProperties);
						writeMethod.invoke(transformationTarget, transformedProperties.get(0));
					}
				} else if (transformedTemplateReadProperty != null) {
					writeMethod.invoke(transformationTarget, transformedTemplateReadProperty);
				}
			}
		}
	}

	private Object transformAny(Object templateReadProperty, TransformationContext ctx) {
		if (templateReadProperty == null) {
			return null;
		}
		if (templateReadProperty instanceof List) {
			List<Object> flattenedList = new ArrayList<>();
			for (Object object : (List<?>) templateReadProperty) {
				Object partialResult = transformAny(object, ctx);
				if (partialResult instanceof Collection) {
					flattenedList.addAll((Collection<Object>) partialResult);
				} else {
					flattenedList.add(partialResult);
				}
			}
			return flattenedList;
		}
		if (!(templateReadProperty instanceof Template)) {
			return templateReadProperty;
		} else {
			return inflateTemplate((Template) templateReadProperty, ctx);
		}
	}

	private Class<?> getAASInterface(Object obj) {
		return ReflectionHelper.getAasInterface(obj.getClass());
	}

	private Object newDefaultAASInstance(Class<?> aasInterface) {
		Class<?> defaultImplementation = ReflectionHelper.getDefaultImplementation(aasInterface);
		try {
			return defaultImplementation.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			LOGGER.error("Not able to create a default instance for " + defaultImplementation.getName(), e);
			throw new IllegalArgumentException(e);
		}
	}

	private List<Object> asList(Object evaluate) {
		if (evaluate instanceof List<?>) {
			return (List<Object>) evaluate;
		}
		if (evaluate instanceof Iterable<?>) {
			List<Object> iterableToList = new ArrayList<>();
			((Iterable<Object>) evaluate).forEach(iterableToList::add);
			return iterableToList;
		}
		if (evaluate == null) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(evaluate);
		}
	}

}
