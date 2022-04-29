package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.abt.security.SecurityAbtImpl;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TranslationCommandlineAbt extends TranslationCommandline {
    public TranslationCommandlineAbt() {
        this.generatorBean = new SecurityAbtImpl();
    }

    @Override
    public Map<String,String> generateSecurityPolicyCommandLine(String taskUuid, DeviceFilterlistRO deviceFilterlistRO, List<DeviceFilterRuleListRO> needAddFilter, Map<String, Object> parameterMap) throws Exception {
        if(CollectionUtils.isNotEmpty(needAddFilter)){
            for (DeviceFilterRuleListRO deviceFilterRuleListRO : needAddFilter) {
                deviceFilterRuleListRO.setRuleId(null);
            }
        }
       return super.generateSecurityPolicyCommandLine(taskUuid,deviceFilterlistRO,needAddFilter,parameterMap);
    }
}
