package com.abtnetworks.totems.common.commandline2.constant;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.commandline2.constant.DeviceModelProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author zc
 * @date 2020/01/02
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class DeviceModelPropertiesTest {

    @Resource
    private DeviceModelProperties deviceModelProperties;

    @Test
    public void testDeviceModel() {
        deviceModelProperties.getModelList().forEach(System.out::println);
        deviceModelProperties.getModelMap().forEach((k,v) -> {
            System.out.println("map -> key:" + k);
            System.out.println("map -> value:" + v);
        });
    }

}