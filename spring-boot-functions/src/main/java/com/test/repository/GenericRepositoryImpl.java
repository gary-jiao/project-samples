package com.test.repository;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.HibernateQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.test.repository.FieldPropertyUtils.FieldMapping;

@Repository
@Transactional
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GenericRepositoryImpl implements GenericRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public <C> C findByKey(Class<? extends Model> clazz, Serializable key) {
		return (C) entityManager.find(clazz, key);
	}

	@Override
	public <C> List<C> findByFields(Class<? extends Model> clazz, Model model, String... fields) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery query = builder.createQuery(clazz);
		Root root = query.from(clazz);

		Predicate[] pre = new Predicate[fields.length];
		for (int i = 0; i < fields.length; i++) {
			String fdName = fields[i];
			try {
				pre[i] = builder.equal(root.get(fdName), PropertyUtils.getProperty(model, fdName));
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		query.where(builder.and(pre));
		List<C> list = entityManager.createQuery(query.select(root)).getResultList();

		return list;
	}

	@Override
	public <C> C findUniqueResultByFields(Class<? extends Model> clazz, Model model, String... fields) {
		List<C> list = findByFields(clazz, model, fields);
		if (CollectionUtils.isEmpty(list))
			return null;
		if (list.size() > 1)
			throw new RuntimeException("More than one result have been found!");
		return list.get(0);
	}

	public List<Map> findResultWithMap(String sql, QueryCriteria... criteria) {
		Query query = entityManager.createNativeQuery(sql);
		if (criteria != null) {
			for (QueryCriteria cri : criteria) {
				query.setParameter(cri.getName(), cri.getValue());
			}
		}
		((HibernateQuery)query).getHibernateQuery().setResultTransformer(new AliasToCaseInsensitiveMapResultTransformer());
		return query.getResultList();
	}
	
	public <C> List<C> findResults(Class<C> clazz, String sql, QueryCriteria... criteria) {
		Query query = entityManager.createNativeQuery(sql);
		if (criteria != null) {
			for (QueryCriteria cri : criteria) {
				query.setParameter(cri.getName(), cri.getValue());
			}
		}
		ResultTransformer transformer = null;
		if (Map.class.isAssignableFrom(clazz)) {
			transformer = new AliasToCaseInsensitiveMapResultTransformer();
		} else {
			transformer = new CustomAliasToBeanResultTransformer(clazz);
		}
		((HibernateQuery)query).getHibernateQuery().setResultTransformer(transformer);
		return query.getResultList();
	}

	/**
	 * 将SQL的返回结果构造成Map对象，并且键值不区分大小写
	 * 
	 */
	@SuppressWarnings("serial")
	protected class AliasToCaseInsensitiveMapResultTransformer implements ResultTransformer {
		public AliasToCaseInsensitiveMapResultTransformer() {
			super();
		}

		public Object transformTuple(Object[] tuple, String[] aliases) {
			Map result = new CaseInsensitiveMap(tuple.length);
			for (int i = 0; i < tuple.length; i++) {
				String alias = aliases[i];
				if (!StringUtils.isEmpty(alias)) {
					result.put(alias, tuple[i]);
				}
			}
			return result;
		}

		public List transformList(List collection) {
			return collection;
		}
	}
	
	/**
	 * Hibernate自带的AliasToBeanResultTransformer在使用时，必须先在应用里手工对每个字段设置Scalar，这样使得代码层面不美观，
	 * 而且带来重复工作量。重新实现的这个Transformer，主要是用来避免写这种重复性代码，对数据类型进行自我判断
	 * @author 310078815
	 *
	 */
	protected class CustomAliasToBeanResultTransformer extends AliasToBeanResultTransformer {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 356792605759007440L;
		protected Class resultClass;
		
		public CustomAliasToBeanResultTransformer(Class resultClass) {
			super(resultClass);
			this.resultClass = resultClass;
		}
		
		/**
		 * 在调用父类的方法前，先对tuple里的参数进行类型转换 <br/>
		 * 目前只支持以下几种类型的转换：Long, Integer, Double, Float, Date, String <br/>
		 * 因为Oracle的查询语句会默认将所有返回的字段名全部转换为大写的，所以在定义字段时，一定需要将返回的字段名放在注解@Column里
		 */
		public Object transformTuple(Object[] tuple, String[] aliases) {
			if (ArrayUtils.isEmpty(tuple) || ArrayUtils.isEmpty(aliases))
				return super.transformTuple(tuple, aliases);
			
			//如果aliases对应的名称在resultClass里不存在，则寻找resultClass里每个字段的Column注解，根据注解找到对应的属性名称
			Field[] fields = FieldPropertyUtils.getFields(resultClass);
			boolean exist = false;
			for (String alias : aliases) {
				exist = false;
				for (Field f : fields) {
					if (StringUtils.equals(f.getName(), alias)) {
						exist = true;
						break;
					}
				}
				//只要有一个别名没有找到对应的字段名，则认为字段名称需要通过annotation来查找
				if (!exist) break;
			}
			
			String[] aliases2 = Arrays.copyOf(aliases, aliases.length);
			Object[] tuple2 = Arrays.copyOf(tuple, tuple.length);
			if (!exist) {
				FieldMapping fieldMapping = FieldPropertyUtils.getFieldAnnoMapping(resultClass);
				
				//找到aliaes中存在，但fieldMapping里不存在的字段在数组里的索引
				Stack<Integer> idxs = new Stack<Integer>();
				for (int i = 0; i < aliases.length; i++) {
					String s = fieldMapping.get(aliases[i]);
					aliases2[i] = (s == null ? aliases[i] : s);
					
					if (s == null) idxs.push(i);
				}
				
				if (!idxs.isEmpty()) {
					//从后往前删除，确保删除后面的元素之后，之前元素的索引不会改变
					Integer idx = idxs.pop();
					while (idx != null) {
						aliases2 = ArrayUtils.remove(aliases2, idx);
						tuple2 = ArrayUtils.remove(tuple2, idx);
						
						if (idxs.isEmpty()) break;
						idx = idxs.pop();
					}
				}
			}
			
			if (tuple2.length != aliases2.length)
				return super.transformTuple(tuple, aliases);
			
			PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(resultClass);
			for (int i = 0; i < aliases2.length; i++) {
				PropertyDescriptor pd = locateDescriptor(pds, aliases2[i]);
				if (pd == null) continue;
				Class type = pd.getPropertyType();
				
				if (tuple2[i] == null) continue;
				
				tuple2[i] = DataTypeConverter.convert(tuple2[i], type);
			}
			
			return super.transformTuple(tuple2, aliases2);
		}
		
		protected PropertyDescriptor locateDescriptor(PropertyDescriptor[] pds, String propertyName) {
			for (PropertyDescriptor pd : pds) {
				if (StringUtils.equals(pd.getName(), propertyName))
					return pd;
			}
			return null;
		}

	}

	@Override
	public void save(Model entity) {
		entityManager.persist(entity);
	}

	@Override
	public void update(Model entity) {
		entityManager.merge(entity);
	}

}
