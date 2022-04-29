package com.abtnetworks.totems.common.commandline;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.advanced.dao.mysql.AdvanceSettingsMapper;
import com.abtnetworks.totems.advanced.entity.AdvanceSettingsEntity;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.commandline.rollback.acl.RollbackAclCiscoIos;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.dto.commandline.CommandlineDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.utils.DateUtils;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author zc
 * @date 2020/01/20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
@Log4j2
public class SecurityPolicyFactoryTest {

    @Autowired
    SecurityPolicyFactory securityPolicyFactory;

    @Autowired
    Map<String, PolicyGenerator> policyGeneratorMap;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    private AdvanceSettingsMapper advanceSettingsMapper;

    @Test
    public void securityPolicyFactory() {
        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setName("6");
        commandlineDTO.setSrcIp("0.0.0.0/24,2.1.1.1/24,2.2.2.2/24,3.1.1.1-3.1.1.10");
        commandlineDTO.setDstZone("trust");
        commandlineDTO.setSrcZone("trust");
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
        commandlineDTO.setAction("deny");
        commandlineDTO.setMoveSeatEnum(MoveSeatEnum.FIRST);

//        SecurityPolicyGenerator securityPolicyGenerator = securityPolicyFactory.securityPolicyFactory("usg7000");
        SecurityPolicyGenerator securityPolicyGenerator = securityPolicyFactory.securityPolicyFactory("H3C SecPath V5");
        String result = securityPolicyGenerator.generateCommandline(commandlineDTO);
        System.out.println(result);
    }

    @Test
    public void aclPolicyFactory() {
        CommandlineDTO commandlineDTO = new CommandlineDTO();
        commandlineDTO.setName("test-ACL");
        commandlineDTO.setSrcIp("1.1.1.0-1.1.1.5,1.1.1.2/24");
        commandlineDTO.setDstIp("2.2.2.1/30,2.2.2.2/31");

        commandlineDTO.setStartTime(DateUtils.formatDateTime(DateUtils.parseDate("2021.04.19 20:10:12")));
        commandlineDTO.setEndTime(DateUtils.formatDateTime(new Date()));
        commandlineDTO.setDstZone("trust");
        commandlineDTO.setSrcZone("trust");
        commandlineDTO.setDescription("test-acl");
        commandlineDTO.setRuleListName("this_is_ruleName");
        commandlineDTO.setPolicyId("7");

        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("6");
        serviceDTO1.setSrcPorts("0");
        serviceDTO1.setDstPorts("1,2-5,8");

//        ServiceDTO serviceDTO1 = new ServiceDTO();
//        serviceDTO1.setProtocol("1");

        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("17");
        serviceDTO2.setSrcPorts("0");
        serviceDTO2.setDstPorts("80-85");
        commandlineDTO.setServiceList(Arrays.asList(serviceDTO1,serviceDTO2));
        commandlineDTO.setAction("permit");

        SecurityPolicyGenerator securityPolicyGenerator = securityPolicyFactory.securityPolicyFactory("Cisco IOS");
        String result = securityPolicyGenerator.generateCommandline(commandlineDTO);
        System.out.println(result);
    }

    @Test
    public void aclRollbackPolicyFactory() {
        CmdDTO cmdDTO = new CmdDTO();
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setRuleListName("test-rollback-name");
        SettingDTO settingDTO = new SettingDTO();
        List<Integer> ruleIds = new ArrayList<>();
        ruleIds.add(1);
        ruleIds.add(2);
        ruleIds.add(3);
        ruleIds.add(4);
        settingDTO.setUsableRuleList(ruleIds);
        cmdDTO.setDevice(deviceDTO);
        cmdDTO.setSetting(settingDTO);
        String name = NameUtils.getServiceDefaultName(RollbackAclCiscoIos.class);
        String generator = NameUtils.firstLowerCase(name);
        log.info("命令行生成器为{}", generator);
        String commandLine = policyGeneratorMap.get(generator).generate(cmdDTO);
        System.out.println("rollback-commandLine is:" + commandLine);
    }

    @Test
    public void addCiscoPolicy() {
        List<Integer> list = new ArrayList<>();
        list.add(21);
        list.add(22);
        JSONObject object = new JSONObject();
        object.put("uuid", "de23232wdasewewdsdwew");
        JSONArray itemJsonArray = new JSONArray();
        JSONObject itemObject = new JSONObject();
        itemObject.put("taskId", "290");
        itemObject.put("id", list);
        JSONObject itemObject2 = new JSONObject();
        itemObject2.put("taskId", "292");
        itemObject2.put("id", list);
        itemJsonArray.add(itemObject);
        itemJsonArray.add(itemObject2);
        object.put("item", itemJsonArray);




        JSONObject object2 = new JSONObject();
        object2.put("uuid", "lifei");
        JSONArray itemJsonArray2 = new JSONArray();
        JSONObject itemObject3 = new JSONObject();
        itemObject3.put("taskId", "290");
        itemObject3.put("id", list);
        JSONObject itemObject4 = new JSONObject();
        itemObject4.put("taskId", "292");
        itemObject4.put("id", list);
        itemJsonArray2.add(itemObject3);
        itemJsonArray2.add(itemObject4);
        object2.put("item", itemJsonArray2);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(object);
        jsonArray.add(object2);
        AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
        entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ROUTE_POLICY_ID);
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.insert(entity);
        System.out.println("sds");
    }

    @Test
    public void deleteCiscoPolicy() {
        advancedSettingService.removeRuleIdByTaskId(290);
        System.out.println("asd");
    }

    @Test
    public void insertCiscoPolicy() {
        List<Integer> list = new ArrayList<>();
        list.add(2123);
        list.add(2222);
        advancedSettingService.setCiscoRoutePolicyId("9d1a2e039bfd478485eeab43bef69e21",list,292);
        System.out.println("asd");
    }

}