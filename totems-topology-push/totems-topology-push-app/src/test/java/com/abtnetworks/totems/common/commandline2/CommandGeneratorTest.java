package com.abtnetworks.totems.common.commandline2;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.commandline2.dto.CommandDTO;
import com.abtnetworks.totems.common.dto.commandline.PolicyObjectDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

/**
 * @author zc
 * @date 2020/01/07
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class CommandGeneratorTest {

    @Resource
    private CommandFactory commandFactory;

    @Test
    public void routerGeneratorCommandLine() {
        String modelNumber = "USG6000";
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setModelNumber(modelNumber);
        commandDTO.setIsVsys(false);
        commandDTO.setIpAddress("1.1.1.1/30,2.2.2.2");

        CommandGenerator commandGenerator = commandFactory.getCommandGenerator(modelNumber);
        String cmd = commandGenerator.routerCreateCommandLine(commandDTO);
        System.out.println(cmd);
    }

    @Test
    public void routerDeleteCommandLine() {
        String modelNumber = "USG6000";
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setModelNumber(modelNumber);
        commandDTO.setIsVsys(false);
        commandDTO.setIpAddress("1.1.1.1/30,ef::32");

        CommandGenerator commandGenerator = commandFactory.getCommandGenerator(modelNumber);
        String cmd = commandGenerator.routerDeleteCommandLine(commandDTO);
        System.out.println(cmd);
    }


    @Test
    public void addressObjectListCreate() {
        String modelNumber = "USG6000";
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setModelNumber(modelNumber);
        commandDTO.setTicket("ticket");
        commandDTO.setIpAddress("1.1.1.1,2.2.2.2/24,3.3.3.3-3.3.3.6");
        CommandGenerator commandGenerator = commandFactory.getCommandGenerator(modelNumber);
        List<PolicyObjectDTO> policyObjectDTOList = commandGenerator.addressObjectListCreate(commandDTO);
        System.out.println(policyObjectDTOList);
    }

    @Test
    public void serviceObjectListCreate() {
        String modelNumber = "USG6000";
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setModelNumber(modelNumber);

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setDstPorts("44");

        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("22-33,255");

        commandDTO.setServiceList(Arrays.asList(serviceDTO1, serviceDTO2));
        CommandGenerator commandGenerator = commandFactory.getCommandGenerator(modelNumber);
        List<PolicyObjectDTO> policyObjectDTOList = commandGenerator.serviceObjectListCreate(commandDTO);
        System.out.println(policyObjectDTOList);
    }

    @Test
    public void timeObjectListCreate() {
        String modelNumber = "USG6000";
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setModelNumber(modelNumber);
        commandDTO.setTicket("ticket");
        commandDTO.setStartTime("2020-1-1 10:20:30");
        commandDTO.setEndTime("2020-10-10 1:2:3");

        CommandGenerator commandGenerator = commandFactory.getCommandGenerator(modelNumber);
        List<PolicyObjectDTO> policyObjectDTOList = commandGenerator.timeObjectListCreate(commandDTO);
        System.out.println(policyObjectDTOList);
    }

    @Test
    public void secPolicyCommandLine() {
        String modelNumber = "USG6000";
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setModelNumber(modelNumber);
        commandDTO.setTicket("ticket");
        commandDTO.setName("name");
        commandDTO.setDescription("description");
        commandDTO.setAction("deny");
        commandDTO.setSrcIp("1.1.1.1,2.2.2.2/24,3.3.3.3-3.3.3.6");
        commandDTO.setDstIp("4.4.4.4,5.5.5.5/24,6.6.6.6-6.6.6.9");

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setDstPorts("44");

        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setDstPorts("22");
        commandDTO.setServiceList(Arrays.asList(serviceDTO1, serviceDTO2));

        commandDTO.setStartTime("2020-1-1 10:20:30");
        commandDTO.setEndTime("2020-10-10 1:2:3");

        CommandGenerator commandGenerator = commandFactory.getCommandGenerator(modelNumber);
        String cmd = commandGenerator.secPolicyCommandLine(commandDTO);
        System.out.println(cmd);
    }

}