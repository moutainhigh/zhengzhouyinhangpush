package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;

import com.abtnetworks.totems.push.dto.policy.PolicyInfoDTO;
import com.abtnetworks.totems.push.service.PushBussExtendService;
import com.abtnetworks.totems.push.service.PushTaskBusinessArithmeticService;
import com.abtnetworks.totems.push.vo.FivePushInfoVo;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/4/29
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushTaskBusinessArithmeticServiceTest {
    @Resource
    PushTaskBusinessArithmeticService pushTaskBusinessArithmeticService;

    @Resource
    PushBussExtendService pushBussExtendService;

    @Test
    public void descartesSplitQuintuple() {
        List<PolicyInfoDTO> quintetList0 = new ArrayList<>();
        PolicyInfoDTO newPolicyPushVO0 = new PolicyInfoDTO();
        newPolicyPushVO0.setSrcIp("9.4.6.0/24");
        newPolicyPushVO0.setDstIp("8.6.3.11");

        List<ServiceDTO> serviceList0 = new ArrayList<>();
        ServiceDTO serviceDTO0 = new ServiceDTO();
        serviceDTO0.setProtocol("17");
        serviceDTO0.setDstPorts("2001");
        serviceList0.add(serviceDTO0);
        newPolicyPushVO0.setServiceList(serviceList0);
        quintetList0.add(newPolicyPushVO0);
        List<PolicyInfoDTO> quintetList = new ArrayList<>();
        PolicyInfoDTO newPolicyPushVO = new PolicyInfoDTO();
        newPolicyPushVO.setSrcIp("9.4.6.0/24");
        newPolicyPushVO.setDstIp("8.6.3.12-8.6.3.20");

        List<ServiceDTO> serviceList = new ArrayList<>();
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setProtocol("0");

        serviceList.add(serviceDTO);
        newPolicyPushVO.setServiceList(serviceList);
        quintetList.add(newPolicyPushVO);
        PolicyInfoDTO newPolicyPushVO1 = new PolicyInfoDTO();
        newPolicyPushVO1.setSrcIp("9.4.6.0/24");
        newPolicyPushVO1.setDstIp("8.6.3.11");

        List<ServiceDTO> serviceList1 = new ArrayList<>();
        ServiceDTO serviceDTO1 = new ServiceDTO();
        serviceDTO1.setProtocol("0");
        serviceList1.add(serviceDTO1);
        newPolicyPushVO1.setServiceList(serviceList1);
        quintetList.add(newPolicyPushVO1);

        PolicyInfoDTO newPolicyPushVO21 = new PolicyInfoDTO();
        newPolicyPushVO21.setSrcIp("9.4.6.0/24");
        newPolicyPushVO21.setDstIp("8.6.3.11");

        List<ServiceDTO> serviceList21 = new ArrayList<>();
        ServiceDTO serviceDTO21 = new ServiceDTO();
        serviceDTO21.setProtocol("0");
        serviceDTO21.setDstPorts("11,12");
        serviceList21.add(serviceDTO21);
        newPolicyPushVO21.setServiceList(serviceList21);
        quintetList.add(newPolicyPushVO21);

        PolicyInfoDTO newPolicyPushVO2 = new PolicyInfoDTO();
        newPolicyPushVO2.setSrcIp("9.4.6.0/24");
        newPolicyPushVO2.setDstIp("8.6.3.1-8.6.3.10");

        List<ServiceDTO> serviceList2 = new ArrayList<>();
        ServiceDTO serviceDTO2 = new ServiceDTO();
        serviceDTO2.setProtocol("0");
        serviceList2.add(serviceDTO2);
        newPolicyPushVO2.setServiceList(serviceList2);
        quintetList.add(newPolicyPushVO2);

        PolicyInfoDTO newPolicyPushVO3 = new PolicyInfoDTO();
        newPolicyPushVO3.setSrcIp("9.4.6.0/24");
        newPolicyPushVO3.setDstIp("8.6.3.1-8.6.3.10");

        List<ServiceDTO> serviceList3 = new ArrayList<>();
        ServiceDTO serviceDTO3 = new ServiceDTO();
        serviceDTO3.setProtocol("17");
        serviceDTO3.setDstPorts("11,12");
        serviceList3.add(serviceDTO3);
        newPolicyPushVO3.setServiceList(serviceList3);
        quintetList.add(newPolicyPushVO3);
        List<PolicyInfoDTO> newPolicyPushVOList = pushTaskBusinessArithmeticService.sameTaskMergeQuintuple(quintetList);
        log.error(JSONObject.toJSONString(newPolicyPushVOList));

    }
    @Test
    public void sameTaskMergeQuintuple() {
    }


    /**查询地址组对象**/
    @Test
    public void getNewWorkGroupObjectTest() {
        String deviceUuid = "6eacb378bf7a4776b02f300b5182f9d7";
        int psize = 20;
        int page = 1;
        DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();
        searchDTO.setDeviceUuid(deviceUuid);
        searchDTO.setPsize(psize);
        searchDTO.setPage(page);
        FivePushInfoVo resultDTO = pushBussExtendService.queryPoolNameForFive(deviceUuid);
        System.out.println("原始返回:" + JSONObject.toJSONString(resultDTO));
    }
}
