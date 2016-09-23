package com.test.repository;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NativeQueryInterceptor {

	// @Around("@annotation(org.springframework.data.jpa.repository.Query)" )
	@Around("execution(* com.test.**.repository.*.*(..))")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature ms = (MethodSignature) pjp.getSignature();
		Query query = ms.getMethod().getAnnotation(Query.class);
		if (query == null || !query.nativeQuery() || StringUtils.isEmpty(query.name()))
			return pjp.proceed();

		// 目前只支持解析所有参数都是同一类型的，例如要么全部是@Param，要么全部是QueryCriteria，不支持混合模式
		Object[] args = pjp.getArgs();
		
		if (ArrayUtils.isEmpty(args))
			return genericRepository.findResults(Class.forName(query.name()), query.value());

		if (args[0] instanceof QueryCriteria) {
			return genericRepository.findResults(Class.forName(query.name()), query.value(), (QueryCriteria[]) args);
		}

		List<QueryCriteria> argList = new ArrayList<>();
		Annotation[][] annotations = ms.getMethod().getParameterAnnotations();
		for (int i = 0; i < annotations.length; i++) {
			Annotation[] anno = annotations[i];
			
			Param param = getParamAnno(anno);
			if (param == null) continue;
			
			argList.add(new QueryCriteria(param.value(), args[i]));
		}
		return genericRepository.findResults(Class.forName(query.name()), query.value(), argList.toArray(new QueryCriteria[argList.size()]));
	}
	
	private Param getParamAnno(Annotation[] annos) {
		if (ArrayUtils.isNotEmpty(annos)) {
			for (Annotation anno : annos) {
				if (anno.annotationType().isAssignableFrom(Param.class)) return (Param)anno;
			}
		}
		return null;
	}

	@Autowired
	private GenericRepository genericRepository;
}
