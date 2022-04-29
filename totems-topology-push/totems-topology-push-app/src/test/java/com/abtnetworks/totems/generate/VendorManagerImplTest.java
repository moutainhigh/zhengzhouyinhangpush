package com.abtnetworks.totems.generate;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.ProcedureDTO;
import com.abtnetworks.totems.generate.manager.VendorManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class VendorManagerImplTest {

    @Autowired
    VendorManager endorManager;

    @Test
    public void getVendorInfo(){
        CmdDTO cmdDTO = new CmdDTO();
        PolicyDTO policy = new PolicyDTO();
        policy.setType(PolicyEnum.SECURITY);
        cmdDTO.setPolicy(policy);
        ProcedureDTO procedure = new ProcedureDTO();
        cmdDTO.setProcedure(procedure);
        DeviceDTO _dev = new DeviceDTO();
        _dev.setModelNumber(DeviceModelNumberEnum.CISCO);
        cmdDTO.setDevice(_dev);
        endorManager.getVendorInfo(cmdDTO);
    }
}
