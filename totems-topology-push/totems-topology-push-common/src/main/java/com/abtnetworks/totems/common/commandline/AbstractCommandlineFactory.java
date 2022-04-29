package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.commandline.routing.RoutingGenerator;

/**
 * @author zc
 * @date 2019/11/12
 */
public interface AbstractCommandlineFactory {

    SecurityPolicyGenerator securityPolicyFactory(String vendor);

    NatPolicyGenerator natPolicyFactory(String vendor);

    RoutingGenerator routingFactory(String vendor);

    /**
     * 根据型号，得到回滚命令行类
     * @param modelNumber
     * @return
     */
    PolicyGenerator rollbackSecurity(String modelNumber);
}
