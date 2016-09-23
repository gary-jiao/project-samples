package com.test.repository;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Add from v0.5.0 <br/>
 * PropertyUtils 
 * 
 * @author $Author: gary.jiao $
 * @version $Revision: 496 $
 * @since $Date: 2014-07-25 09:17:54 +0800 (Fri, 25 Jul 2014) $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class FieldPropertyUtils {

	/**
	 * 找到指定对象所有字段名称和注解名称之间的映射
	 * @param clazz
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	public static FieldMapping getFieldAnnoMapping(Class clazz) {
		FieldMapping mapping = new FieldMapping();
		
		PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(clazz);
		for (PropertyDescriptor pd : pds) {
			if (StringUtils.equals(pd.getName(), "class")) continue;
			
			Field f = FieldUtils.getField(clazz, pd.getName(), true);
			if (f == null) continue;
			
			Column col = f.getAnnotation(Column.class);
			//如果属性的定义没有annotation信息，则寻找readMethod
			if (col == null && pd.getReadMethod() != null) {
				col = pd.getReadMethod().getAnnotation(Column.class);
			}
			
			if (col == null) {
				mapping.add(pd.getName(), pd.getName());
			} else {
				mapping.add(col.name(), pd.getName());
			}
		}
		
		return mapping;
	}
	
	public static class FieldMapping {
		private Map<String, String> annoMapping;
		private Map<String, String> propMapping;
		
		public FieldMapping() {
			this.annoMapping = new CaseInsensitiveMap();
			this.propMapping = new CaseInsensitiveMap();
		}
		
		public FieldMapping add(String key, String value) {
			this.annoMapping.put(key, value);
			this.propMapping.put(value, value);
			return this;
		}
		
		public Map<String, String> getAnnoMapping() {
			return this.annoMapping;
		}
		
		public Map<String, String> getPropMapping() {
			return this.propMapping;
		}
		
		public String get(String key) {
			return annoMapping.get(key) != null ? annoMapping.get(key) : propMapping.get(key);
		}
 	}
	
	/**
	 * 获取指定类的所有定义过的字段，包括所有父类里定义的字段
	 * @param clazz
	 * @return
	 */
	public static Field[] getFields(Class clazz) {
		Map<String, Field> fieldMap = new HashMap<String, Field>();
		for (Class<?> acls = clazz; acls != null; acls = acls.getSuperclass()) {
            Field[] fields = acls.getDeclaredFields();
            for (Field f : fields) {
            	if (fieldMap.containsKey(f.getName())) continue;
            	fieldMap.put(f.getName(), f);
            }
        }
		
		for (Class<?> class1 : ClassUtils.getAllInterfaces(clazz)) {
            Field[] fields = ((Class<?>) class1).getFields();
            for (Field f : fields) {
            	if (fieldMap.containsKey(f.getName())) continue;
            	fieldMap.put(f.getName(), f);
            }
        }
		
		return fieldMap.values().toArray(new Field[fieldMap.size()]);
	}
}
