package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.commandline.routing.*;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zc
 * @date 2019/11/15
 */
@Slf4j
public class RoutingFactory implements AbstractCommandlineFactory {
    @Override
    public SecurityPolicyGenerator securityPolicyFactory(String vendor) {
        return null;
    }

    @Override
    public NatPolicyGenerator natPolicyFactory(String vendor) {
        return null;
    }

    @Override
    public RoutingGenerator routingFactory(String modelNumber) {
        log.info("调用路由命令行对象，型号为：[{}]", modelNumber);
        if (StringUtils.isBlank(modelNumber)) {
            log.error("型号为空");
            return null;
        }

        DeviceModelNumberEnum modelNumberEnum = DeviceModelNumberEnum.fromString(modelNumber);
        if (modelNumberEnum == DeviceModelNumberEnum.DEFAULTMODEL) {
            log.error("匹配到了默认设备，跳过处理, modelNumber:{}", modelNumber);
            return null;
        }
        Class routingClass = modelNumberEnum.getRoutingClass();

        if (routingClass == null) {
            log.error("型号暂不支持生成路由命令行,modelNumber:{}", modelNumber);
            return null;
        }

        RoutingGenerator routingObject = null;

        try {
            routingObject = (RoutingGenerator) routingClass.newInstance();
        } catch (Exception e) {
            log.error("获取路由命令行对象异常", e);
        }
        return routingObject;
    }

    @Override
    public PolicyGenerator rollbackSecurity(String modelNumber) {
        return null;
    }
}
