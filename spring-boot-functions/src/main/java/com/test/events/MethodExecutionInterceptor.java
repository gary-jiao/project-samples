package com.test.events;

import java.util.Date;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodExecutionInterceptor {
	
	@Autowired
	private MethodExecutionLogger execLogger;
	
	@Value("${method.event.enable:false}")
	private boolean isEnable;

	@Around("execution(* com.test.**.controllers.*.*(..)) "
			+ " || execution(* com.test.**.service.*.*(..))"
			+ " || execution(* com.test.**.repository.*.*(..))")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		if (!isEnable) return pjp.proceed();
		
		MethodExecutionModel model = new MethodExecutionModel();
		model.setThreadName(Thread.currentThread().getName() + Thread.currentThread().getId());
		model.setClassName(pjp.getTarget().getClass().getName());
		model.setMethodName(pjp.getSignature().getName());
		model.setStartTime(new Date());
		execLogger.start(model);
		
		try {
			return pjp.proceed();
		} catch (Exception ex) {
			model.setStatus("Error");
			throw ex;
		} finally {
			if (model.getStatus() == null) {
				model.setStatus("Ok");
			}
			model.setEndTime(new Date());
			model.setDuration(model.getEndTime().getTime() - model.getStartTime().getTime());
			execLogger.end(model);
		}
	}
}
