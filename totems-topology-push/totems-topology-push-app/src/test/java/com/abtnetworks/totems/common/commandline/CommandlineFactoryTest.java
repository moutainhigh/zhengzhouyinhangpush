package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author zc
 * @date 2019/11/13
 */
public class CommandlineFactoryTest {

    public static void main(String[] args) {
        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setBusinessName("test-name");
        commandlineDTO.setName("6");
        commandlineDTO.setSrcIp("0.0.0.0/24,2.1.1.1/24,2.2.2.2/24,3.1.1.1-3.1.1.10");
        commandlineDTO.setDstZone("trust");
        commandlineDTO.setSrcZone("trust");
        commandlineDTO.setFirstPolicyName("5");
        commandlineDTO.setDescription("aaaaa");

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setDstPorts("1,2-5,8");

//        ServiceDTO serviceDTO1 = new ServiceDTO();
//        serviceDTO1.setProtocol("1");

        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("any");
        commandlineDTO.setServiceList(Arrays.asList(serviceDTO1,serviceDTO2));
//        Assert.assertNotNull(serviceDTO.getType());
        commandlineDTO.setAction("deny");
        commandlineDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);
        AbstractCommandlineFactory commandlineFactory = new SecurityPolicyFactory();
        SecurityPolicyGenerator securityPolicyGenerator = commandlineFactory.securityPolicyFactory("H3C SecPath V5");
        String result = securityPolicyGenerator.composite(commandlineDTO);
        System.out.println(result);

    }

}