package com.abtnetworks.totems.generate.aspect;

import com.abtnetworks.totems.common.annotation.CustomCli;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ProcedureDTO;
import com.abtnetworks.totems.common.utils.NameUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义命令行注解拦截
 * @author lxq
 * @date 2020-03-22
 */
@Aspect
@Component
public class CustomCliAspect {
    private final Logger LOGGER = LoggerFactory.getLogger(CustomCliAspect.class);

    @Autowired
    private ApplicationContext applicationContext;

    /***
     * 命令行组装的切点
     */
    @Pointcut("@annotation(com.abtnetworks.totems.common.annotation.GenerateCliBuilder)")
    public void operationPointGenerateCli() {
    }


    @After(value = "operationPointGenerateCli()")
    public void afterReturningOperation(JoinPoint joinPoint) {
        Object[] objects = joinPoint.getArgs();
        DeviceDTO deviceDTO = (DeviceDTO) objects[1];
        DeviceModelNumberEnum deviceModelNumberEnum = deviceDTO.getModelNumber();
        //获取带有@CustomCli的注解类
        PolicyEnum policyType = (PolicyEnum) objects[0];
        ProcedureDTO procedure = (ProcedureDTO) objects[2];
        final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(CustomCli.class);
        for (String key : beansWithAnnotation.keySet()) {
            Class clazz = beansWithAnnotation.get(key).getClass();

            //获取该类的CustomCli注解对象
            CustomCli customCli = AnnotationUtils.findAnnotation(clazz, CustomCli.class);
            DeviceModelNumberEnum dmne = customCli.value();
            PolicyEnum policyEnum = customCli.type();
            Class classPoxy = customCli.classPoxy();

            //厂商品牌匹配
            if (deviceModelNumberEnum.equals(dmne)) {
                //策略类型匹配
                if(policyType.equals(policyEnum)) {
                    String name = NameUtils.getServiceDefaultName(clazz);
                    String generator = NameUtils.firstLowerCase(name);
                    if(classPoxy.getSimpleName().equals(Class.class.getSimpleName())){
                        //默认值就维持以前的版本，只有安全策略
                        if (procedure.getGenerator() != null) {
                            //得到spring管理容器中的对应bean实例
                            LOGGER.info("使用定制插件中的 命令行生成器为{}", generator);
                            // 插件里的命令行生成类  替换默认的生成类
                            procedure.setGenerator(generator);
                        }
                    }else{
                        //如果不是默认值就需要判断标记的代理类是策略还是回滚
                        String namePoxy = NameUtils.getServiceDefaultName(classPoxy);

                        String generatorPoxy = NameUtils.firstLowerCase(namePoxy);
                        if (procedure.getGenerator() != null && generatorPoxy.equals(procedure.getGenerator())) {
                            //得到spring管理容器中的对应bean实例
                            LOGGER.info("使用定制插件中的 命令行生成器为{}", generator);
                            // 插件里的命令行生成类  替换默认的生成类
                            procedure.setGenerator(generator);
                        }
                        if(procedure.getRollbackGenerator() != null && generatorPoxy.equals(procedure.getRollbackGenerator())){
                            procedure.setRollbackGenerator(generator);
                        }
                    }




                }
            }
        }
    }

}
