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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sap.dsc.aas.lib.expressions.Expression;
import com.sap.dsc.aas.lib.mapping.model.Header;
import com.sap.dsc.aas.lib.mapping.model.MappingSpecification;
import com.sap.dsc.aas.lib.mapping.model.Template;

import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.LangString;

public class AASMappingTransformer {

	private static class InstanceByBindings {
		Object instance;
		List<BeanPropertyDefinition> propertiesByBinding = Collections.emptyList();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Transforms a Template based AssetAdministrationShellEnvironment to a pure
	 * AssetAdministrationShellEnvironment
	 * 
	 * @param mappingSpec        MappingSpecification containing a complete
	 *                           AssetAdministrationShellEnvironment in which all
	 *                           AAS Objects might implement the Template Interface
	 * @param initialContextItem Object which might provide a data context to
	 *                           extract data and transform it into the
	 *                           AssetAdministrationShellEnvironment using Template
	 *                           logic
	 * @return AssetAdministrationShellEnvironment which is the transformation
	 *         result of the Template based attributes
	 */
	public AssetAdministrationShellEnvironment transform(MappingSpecification mappingSpec, Object initialContextItem) {
		TransformationContext initialCtx = createInitialContext(initialContextItem, mappingSpec.getHeader());
		AssetAdministrationShellEnvironment aasEnvTemplate = mappingSpec.getAasEnvironmentMapping();
		if (mappingSpec.getAasEnvironmentMapping() instanceof Template
				&& ((Template) mappingSpec.getAasEnvironmentMapping()).getForeachExpression() != null) {
			LOGGER.warn(
					"@forEach expression on top level AAS Environment is not applicable. Only one AAS Environments will be returned!");
		}
		return (AssetAdministrationShellEnvironment) asList(transformAny(aasEnvTemplate, initialCtx)).get(0);
	}

	private TransformationContext createInitialContext(Object initialContextItem, Header header) {
		return TransformationContext.buildContext(null, initialContextItem, header);
	}

	private List<? extends Object> inflateTemplate(Template template, TransformationContext parentCtx) {
		List<Object> inflated = new ArrayList<>();
		Expression foreachExpression = template.getForeachExpression();
		if (foreachExpression != null) {
			Object evaluate = template.getForeachExpression().evaluate(parentCtx);
			List<Object> forItems = asList(evaluate);
			for (Object forItem : forItems) {
				TransformationContext childCtx = TransformationContext.buildContext(parentCtx, forItem, template);
				inflated.add(transformWithBindings(template, childCtx));
			}
		} else {
			TransformationContext childCtx = TransformationContext.buildContext(parentCtx, parentCtx.getContextItem(),
					template);
			inflated.add(transformWithBindings(template, childCtx));
		}
		return inflated;
	}

	private Object transformWithBindings(Template template, TransformationContext ctx) {
		Class<?> aasInterface = getAASInterface(template);
		InstanceByBindings transformedEntity = null;
		if (template.getBindSpecification() != null) {
			transformedEntity = createInstanceByBindings(template, aasInterface, ctx);
		} else {
			transformedEntity = new InstanceByBindings();
			transformedEntity.instance = newDefaultAASInstance(aasInterface);
		}
		try {
			transformProperties(template, ctx, transformedEntity);
		} catch (NoSuchMethodException | SecurityException | IntrospectionException | IllegalAccessException
				| InvocationTargetException e) {
			LOGGER.error("Failed to transform properties for " + transformedEntity.getClass().getName(), e);
		}
		return transformedEntity.instance;
	}

	private InstanceByBindings createInstanceByBindings(Template template, Class<?> aasInterface,
			TransformationContext ctx) {
		Object transformedEntity;
		Map<String, Object> evaluatedBindings = new HashMap<>();
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
			InstanceByBindings instanceByBindings = new InstanceByBindings();
			instanceByBindings.instance = transformedEntity;
			setPropertiesByBindings(aasInterface, instanceByBindings, evaluatedBindings, jsonMapper);
			return instanceByBindings;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LOGGER.error("Failed to read binding specification, evaluated as '{}' for model '{}'. Error: '{}'",
					evaluatedBindings, aasInterface.getName(), e.getMessage());
			InstanceByBindings instanceByBindings = new InstanceByBindings();
			instanceByBindings.instance = newDefaultAASInstance(aasInterface);
			return instanceByBindings;
		}
	}

	private void setPropertiesByBindings(Class<?> aasInterface, InstanceByBindings instanceByBindings,
			Map<String, Object> evaluatedBindings, JsonMapper jsonMapper) {
		Map<String, BeanPropertyDefinition> propertyDefinitions = findPropertyDefinitions(aasInterface, jsonMapper);
		Set<String> expectedKeys = propertyDefinitions.keySet();
		Set<String> keysOfBinding = evaluatedBindings.keySet();
		for (String keyOfBinding : keysOfBinding) {
			if (!expectedKeys.contains(keyOfBinding)) {
				LOGGER.warn("Key '{}' is used in bindings but {} contains '{}'.", keyOfBinding, aasInterface.getName(),
						expectedKeys);
			}
		}
		List<String> keysNotBinded = expectedKeys.stream().filter(k -> !keysOfBinding.contains(k))
				.collect(Collectors.toList());
		for (String keyNotUsed : keysNotBinded) {
			propertyDefinitions.remove(keyNotUsed);
		}
		instanceByBindings.propertiesByBinding = new ArrayList<>(propertyDefinitions.values());
	}

	private Map<String, BeanPropertyDefinition> findPropertyDefinitions(Class<?> aasInterface, JsonMapper jsonMapper) {
		JavaType constructType = jsonMapper.getTypeFactory().constructType(aasInterface);
		BeanDescription beanDescription = jsonMapper.getSerializationConfig().introspect(constructType);
		Map<String, BeanPropertyDefinition> findProperties = beanDescription.findProperties().stream()
				.collect(Collectors.toMap(bpd -> bpd.getName(), Function.identity()));
		return findProperties;
	}

	private void transformProperties(Template template, TransformationContext ctx,
			InstanceByBindings instanceByBindings)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Object transformationTarget = instanceByBindings.instance;
		PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(transformationTarget.getClass())
				.getPropertyDescriptors();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			Method readMethod = propertyDescriptor.getReadMethod();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			if (readMethod != null && writeMethod != null && notSetByBindings(instanceByBindings, writeMethod)) {
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

	private boolean notSetByBindings(InstanceByBindings instanceByBindings, Method writeMethod) {
		return !instanceByBindings.propertiesByBinding.stream().map(bpd -> bpd.getSetter().getAnnotated().getName())
				.anyMatch(name -> name.equals(writeMethod.getName()));
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
		if (obj instanceof LangString) {
			return LangString.class;
		}
		return ReflectionHelper.getAasInterface(obj.getClass());
	}

	private Object newDefaultAASInstance(Class<?> aasInterface) {
		Class<?> defaultImplementation = ReflectionHelper.getDefaultImplementation(aasInterface);
		try {
			if (defaultImplementation == null && aasInterface.isAssignableFrom(LangString.class)) {
				return LangString.class.getConstructor().newInstance();
			}
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
