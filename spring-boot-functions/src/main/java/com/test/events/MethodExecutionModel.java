package com.test.events;

import java.util.Date;

public class MethodExecutionModel {
	
	private Long oid;
	private String threadName;
	private String className;
	private String methodName;
	private Date startTime;
	private Date endTime;
	private Long duration;
	private String status;

	public Long getOid() {
		return this.oid;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "MethodExecutionModel [oid=" + oid + ", threadName=" + threadName + ", className=" + className
				+ ", methodName=" + methodName + ", startTime=" + startTime + ", endTime=" + endTime + ", duration="
				+ duration + ", status=" + status + "]";
	}
	
}
