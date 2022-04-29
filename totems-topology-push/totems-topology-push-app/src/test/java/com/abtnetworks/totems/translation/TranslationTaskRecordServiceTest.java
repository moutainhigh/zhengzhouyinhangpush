package com.abtnetworks.totems.translation;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.service.TranslationTaskRecordService;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.ServiceGroupObjectRO;
import com.abtnetworks.totems.whale.baseapi.ro.TimeObjectRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/1/12 16:44'.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class TranslationTaskRecordServiceTest {

    @Resource
    private TranslationTaskRecordService translationTaskRecordService;

    @Resource
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Resource
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Test
    public void test1() throws Exception {

        TranslationTaskRecordEntity entity = new TranslationTaskRecordEntity();
        //6847402ebb1a499ca5abe026c97eba83 c917a5dcba3a47c6818f597844b2b3e3 438e2dc278b240c88c99a7a2e29b93ec c1bf0262daa348cfaf4af4e3b4d2ec4c 08f938a924d14352b1d22bc355ce5e1f
        entity.setTitleName("test1 测试");
        entity.setDeviceUuid("6847402ebb1a499ca5abe026c97eba83");
        entity.setTargetDeviceModelNumber("HillstoneStoneOS");

        TranslationTaskRecordEntity recordEntity = translationTaskRecordService.getById(33);
        ReturnT returnT = translationTaskRecordService.startTranslation(recordEntity);
        System.out.println(returnT.toString());
    }

    @Test
    public void test2() {
        DeviceObjectSearchDTO query = new DeviceObjectSearchDTO();
        query.setDeviceUuid("08f938a924d14352b1d22bc355ce5e1f");

        /*//获取时间对象
        ResultRO<List<TimeObjectRO>> timeObjectList = whaleDeviceObjectClient.getTimeObject(query);
        System.out.println(JSON.toJSONString(timeObjectList));
        //获取服务对象
        ResultRO<List<ServiceGroupObjectRO>> serviceObjectList = whaleDeviceObjectClient.getServiceObject(query);
        System.out.println(JSON.toJSONString(serviceObjectList));*/


        //获取服务对象
        ResultRO<List<ServiceGroupObjectRO>> serviceObjectList = whaleDeviceObjectClient.getPredefinedService(query);
        System.out.println(JSON.toJSONString(serviceObjectList));

    }

    public static void main(String[] args) {
        System.out.println(new Date());
    }

}
