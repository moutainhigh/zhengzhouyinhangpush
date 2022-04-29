package com.abtnetworks.totems.commandline;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.common.enums.PolicyTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.translation.enums.TranslationCommandlineEnum;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneR5Impl;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/24
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class GeneralCommandlineTest {

    @Resource
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Resource
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    /**
     * 安全策略命令行生成测试  华为 -->  山石
     * @throws Exception
     */
    @Test
    public void testGenerateSecurityPolicyCommandLine() throws Exception {
        //华为防火墙
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid("2b097fe821f24bec8615e0a06d7e6788");
        DeviceDataRO deviceDataRO = null;
        if(deviceRO != null && CollectionUtils.isNotEmpty(deviceRO.getData())){
            deviceDataRO = deviceRO.getData().get(0);
        } else {
            System.out.println("暂未查询到设备");
            return;
        }

        //华三V7原子化命令行实现类
        TranslationCommandlineEnum translationCommandlineEnum = TranslationCommandlineEnum.fromString("H3C SecPath V7");
        TranslationCommandline translationCommandlineBean = (TranslationCommandline) ConstructorUtils.invokeConstructor(translationCommandlineEnum.getTranslationImplClass());

        //查询策略集
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceDataRO.getUuid());
        StringBuffer commandLine = new StringBuffer();
        StringBuffer warning = new StringBuffer();
        if(resultRO != null && CollectionUtils.isNotEmpty(resultRO.getData())){
            for (DeviceFilterlistRO deviceFilterlistRO : resultRO.getData()) {
                //只生成安全策略
                if (PolicyTypeEnum.SYSTEM__IMPORTED_ROUTING_TABLES.name().equals(deviceFilterlistRO.getRuleListType()) ||
                        PolicyTypeEnum.SYSTEM__ROUTING_TABLES.name().equals(deviceFilterlistRO.getRuleListType()) ||
                        PolicyTypeEnum.SYSTEM__POLICY_ROUTING.name().equals(deviceFilterlistRO.getRuleListType()) ||
                        PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(deviceFilterlistRO.getRuleListType()) ||
                        PolicyTypeEnum.SYSTEM__GENERIC_ACL.name().equals(deviceFilterlistRO.getRuleListType())) {
                    continue;
                }
                //查策略
                ResultRO<List<DeviceFilterRuleListRO>> listResultRO = whaleDevicePolicyClient.getFilterRuleList(deviceDataRO.getUuid(), deviceFilterlistRO.getUuid());
                if(listResultRO != null && CollectionUtils.isNotEmpty(listResultRO.getData())){
                    List<DeviceFilterRuleListRO> needAddFileterRuleList = listResultRO.getData();

                    //生成安全策略命令行
                    Map<String, String> resultMap = translationCommandlineBean.generateSecurityPolicyCommandLine(null,deviceFilterlistRO, needAddFileterRuleList, null);
                    if(StringUtils.isNotBlank(resultMap.get("commandLine"))){
                        commandLine.append(resultMap.get("commandLine"));
                    }
                    if(StringUtils.isNotBlank(resultMap.get("warning"))){
                        warning.append(resultMap.get("warning"));
                    }
                }
            }
        }
        System.out.println(String.format("安全策略命令行: \n %s",commandLine.toString()));
        System.out.println(String.format("命令行生成告警信息: \n %s",warning.toString()));

    }
}
