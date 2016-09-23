package com.test.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.lang3.ClassUtils;

class DataTypeConverter {

	private final static Map<Class<?>, Class<? extends Converter>> CONVERTERS = new HashMap<Class<?>, Class<? extends Converter>>();
	static {
		CONVERTERS.put(Integer.class, IntegerConverter.class);
		CONVERTERS.put(Date.class, DateConverter.class);
		CONVERTERS.put(Long.class, LongConverter.class);
		CONVERTERS.put(Float.class, FloatConverter.class);
		CONVERTERS.put(Double.class, DoubleConverter.class);
		CONVERTERS.put(Boolean.class, BooleanConverter.class);
		CONVERTERS.put(String.class, StringConverter.class);
		CONVERTERS.put(BigDecimal.class, BigDecimalConverter.class);
		CONVERTERS.put(Short.class, ShortConverter.class);
	}
	
	public static <T> T convert(Object value, Class<T> type) {
		if (value == null) return null;
		try {
			Converter converter = null;
			
			Class<?> newType = ClassUtils.primitiveToWrapper(type);
			
			for (Entry<Class<?>, Class<? extends Converter>> entry : CONVERTERS.entrySet()) {
				if (entry.getKey().isAssignableFrom(newType)) {
					converter = entry.getValue().newInstance();
					break;
				}
			}
			if (converter == null) {
				converter = CONVERTERS.get(String.class).newInstance();
			}
			return (T)converter.convert(type, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
