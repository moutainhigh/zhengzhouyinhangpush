package com.abtnetworks.totems.advanced;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.dto.PushMappingNatDTO;
import com.abtnetworks.totems.advanced.dto.SearchPushMappingNatDTO;
import com.abtnetworks.totems.advanced.entity.PushMappingNatEntity;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.advanced.service.PushMappingNatService;
import com.abtnetworks.totems.advanced.vo.DeviceInfoVO;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/9
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class PushMappingNatServiceImplTest {
    @Resource
    PushMappingNatService pushMappingNatService;


    @Autowired
    AdvancedSettingService advancedSettingService;


    @Test
    public void savePushMappingNatInfo() {
        PushMappingNatDTO pushMappingNatDTO = new PushMappingNatDTO();
        pushMappingNatDTO.setPostIp("10.10.10.0-10.10.10.10");
        pushMappingNatDTO.setPreIp("120.120.120.0-120.120.120.120");
        pushMappingNatDTO.setDeviceUuid("15154654698431dfgdg");
        int i = pushMappingNatService.savePushMappingNatInfo(pushMappingNatDTO);
        log.info("保存成功{}", i);

    }

    @Test
    public void deletePushMappingNatInfo() {
        int i = pushMappingNatService.deletePushMappingNatInfo("1");
        log.info("删除成功{}", i);
    }

    @Test
    public void updatePushMappingNatInfo() {
        PushMappingNatDTO pushMappingNatDTO = new PushMappingNatDTO();
        pushMappingNatDTO.setPostIp("10.10.10.0-10.10.10.10");
        pushMappingNatDTO.setPreIp("120.120.120.0-120.120.120.120");
        pushMappingNatDTO.setDeviceUuid("d7f380ee9bf744168797240a0deb16ef");
        pushMappingNatDTO.setId(1);
        int i = pushMappingNatService.updatePushMappingNatInfo(pushMappingNatDTO);
        log.info("修改成功{}", i);
    }

    @Test
    public void listPushMappingNatInfo() {
        SearchPushMappingNatDTO searchPushMappingNatDTO = new SearchPushMappingNatDTO();
        searchPushMappingNatDTO.setCurrentPage(2);
        searchPushMappingNatDTO.setPageSize(20);
        PageInfo<PushMappingNatEntity> pageInfo = pushMappingNatService.listPushMappingNatInfo(searchPushMappingNatDTO);
        log.info("修改成功{}", JSONObject.toJSONString(pageInfo));
    }

    @Test
    public void getbeforeList() {
        String deviceUuid = "6042007e3ef04b62b5682ef6847a931c";
        DeviceDTO beforeConflictDevice = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE, deviceUuid);
        System.out.println(beforeConflictDevice.getDeviceUuid());

        String  deviceUuid2 = "842d671cb87f473aad757c5937770a9c";
        DeviceDTO beforeDevice = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE, deviceUuid2);
        System.out.println(beforeDevice);

        String  deviceUuid3 = "bbee6a616c2a4f039d4d954816eea7e2";
        DeviceDTO afterDevice = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER, deviceUuid3);
        System.out.println(afterDevice);

        String  deviceUuid4 = "47a8a11bc75942df96b1de19c81f26c9";
        DeviceDTO topDevice = advancedSettingService.getMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP, deviceUuid4);
        System.out.println(topDevice);

    }

    @Test
    public void getALlDevice() {
        List<DeviceInfoVO> deviceDTOList = advancedSettingService.getUnselectedSameVendorNameDeviceByJson("sss");
        System.out.println(deviceDTOList);
    }

}