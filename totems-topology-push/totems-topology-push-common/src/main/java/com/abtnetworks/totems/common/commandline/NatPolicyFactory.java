package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.commandline.nat.*;
import com.abtnetworks.totems.common.commandline.routing.RoutingGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zc
 * @date 2019/11/13
 */
@Slf4j
public class NatPolicyFactory implements AbstractCommandlineFactory {

    @Override
    public SecurityPolicyGenerator securityPolicyFactory(String vendor) {
        return null;
    }

    @Override
    public NatPolicyGenerator natPolicyFactory(String vendor) {
        log.info("调用nat策略，型号为：[{}]", vendor);
        if (vendor == null) {
            log.error("型号为空");
            return null;
        }
        switch(vendor) {
            case "USG6000":
                return new U6000();
            case "HillstoneStoneOS":
                return new Hillstone();
            case "Cisco ASA":
                return new Cisco();
            case "Topsec TOS Firewall":
                return new TopsecTOS005Nat();
            default:
                return new Default();
        }
    }

    @Override
    public RoutingGenerator routingFactory(String vendor) {
        return null;
    }

    @Override
    public PolicyGenerator rollbackSecurity(String modelNumber) {
        return null;
    }
}
