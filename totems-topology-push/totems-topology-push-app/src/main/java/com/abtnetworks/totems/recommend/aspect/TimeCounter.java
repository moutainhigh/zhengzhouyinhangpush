package com.abtnetworks.totems.recommend.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class TimeCounter {
    @Pointcut("@annotation(com.abtnetworks.totems.recommend.annotation.TimeCounter)")
    public void costTimePointCut(){}

    @Around("costTimePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        //执行方法
        try {
            result = point.proceed();
        } catch(Exception e) {
            log.error("执行出错...");
        }
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        logCostTime(point, time);
        return result;
    }

    private void logCostTime(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        log.warn("class:"+className+" method:"+methodName + " cost:"+time+"ms");
    }
}
