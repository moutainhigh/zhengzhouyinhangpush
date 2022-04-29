package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.commandline.routing.RoutingGenerator;
import com.abtnetworks.totems.common.commandline.security.*;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author zc
 * @date 2019/11/13
 */
@Slf4j
@Service
public class SecurityPolicyFactory implements AbstractCommandlineFactory {

    @Override
    public SecurityPolicyGenerator securityPolicyFactory(String modelNumber) {
        log.debug("调用安全策略，型号为：[{}]", modelNumber);
        if (StringUtils.isBlank(modelNumber)) {
            log.error("型号为空");
            return null;
        }
        DeviceModelNumberEnum modelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        if (modelNumberEnum == DeviceModelNumberEnum.DEFAULTMODEL) {
            log.error("匹配到了默认设备，跳过处理, modelNumber:{}", modelNumber);
            return null;
        }
        Class security = modelNumberEnum.getSecurityClass();

        if(security == null){
            log.error("型号暂不支持生成命令行,modelNumber:{}", modelNumber);
            return null;
        }

        SecurityPolicyGenerator securityObject = null;
        try {
            securityObject = (SecurityPolicyGenerator) security.newInstance();
        } catch (Exception e) {
            log.error("获取安全策略命令行对象异常", e);
        }
        return securityObject;
    }

    @Override
    public NatPolicyGenerator natPolicyFactory(String vendor) {
        return null;
    }

    @Override
    public RoutingGenerator routingFactory(String vendor) {
        return null;
    }

    @Override
    public PolicyGenerator rollbackSecurity(String modelNumber) {
        log.info("调用安全策略回滚，型号为：[{}]", modelNumber);
        if (StringUtils.isBlank(modelNumber)) {
            log.error("型号为空");
            return null;
        }
        DeviceModelNumberEnum modelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        if (modelNumberEnum == DeviceModelNumberEnum.DEFAULTMODEL) {
            log.error("匹配到了默认设备，跳过处理, modelNumber:{}", modelNumber);
            return null;
        }
        Class security = modelNumberEnum.getRollbackSecurityClass();

        if (security == null) {
            log.error("型号暂不支持生成回滚命令行,modelNumber:{}", modelNumber);
            return null;
        }

        PolicyGenerator rollbackObject = null;
        try {
            rollbackObject = (PolicyGenerator) security.newInstance();
        } catch (Exception e) {
            log.error("获取安全策略回滚命令行对象异常", e);
        }
        return rollbackObject;
    }

}
