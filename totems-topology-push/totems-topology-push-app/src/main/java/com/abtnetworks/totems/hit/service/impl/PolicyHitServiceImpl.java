package com.abtnetworks.totems.hit.service.impl;

import com.abtnetworks.totems.command.line.abs.OverAllGeneratorAbstractBean;
import com.abtnetworks.totems.command.line.enums.DeviceModelNumberEnumExtended;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.PolicyRowUilts;
import com.abtnetworks.totems.external.vo.PolicyDetailVO;
import com.abtnetworks.totems.hit.service.PolicyHitService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRawTextsRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PolicyHitServiceImpl implements PolicyHitService {

    private static Logger logger = Logger.getLogger(PolicyHitServiceImpl.class);

    @Resource
    private NodeMapper nodeDao;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Override
    public TotemsReturnT<Map<String,Object>> generate(DeviceFilterRuleListRO vo){

        Map<String,Object> map = new HashMap<>();
        String deviceUUID = vo.getDeviceUuid();
        NodeEntity entity = nodeDao.getTheNodeByUuid(deviceUUID);

        String deviceName = entity.getDeviceName();
        map.put("deviceName",deviceName);
        map.put("deviceUuid",deviceUUID);
        map.put("editRevertUserName",null);
        map.put("editTime",null);
        map.put("editUserName",null);
        map.put("editable",false);
        map.put("editableRevert",false);
        map.put("id",null);
        map.put("pushResult",null);
        map.put("pushStatus",null);
        map.put("revert",null);
        map.put("revertEcho",null);
        map.put("revertModifiedTime",null);
        map.put("taskId",null);


        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUUID);
        if(deviceRO==null || deviceRO.getData()==null || deviceRO.getData().size()==0){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "设备信息不正确");
        }

        String modelNumber = entity.getModelNumber();
        DeviceModelNumberEnumExtended deviceModelNumberEnumExtended = DeviceModelNumberEnumExtended.fromString(modelNumber);
        if(deviceModelNumberEnumExtended == null || deviceModelNumberEnumExtended.getSecurityClass() == null){
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }
        OverAllGeneratorAbstractBean overAllGeneratorAbstractBean = null;
        try {
            overAllGeneratorAbstractBean = (OverAllGeneratorAbstractBean) ConstructorUtils.invokeConstructor(deviceModelNumberEnumExtended.getSecurityClass());
        } catch (Exception e) {
            log.error("构造对象异常:",e);
            return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "该设备暂不支持");
        }

        //存入配置信息，思科删除命令行时使用
        if ((deviceModelNumberEnumExtended.getKey() == DeviceModelNumberEnumExtended.CISCO_ASA_99.getKey() ||
                deviceModelNumberEnumExtended.getKey() == DeviceModelNumberEnumExtended.CISCO.getKey()) &&
                deviceRO.getData()!= null && deviceRO.getData().size()>0 && StringUtils.isNotBlank(deviceRO.getData().get(0).getRawConfigTextRefId())) {
            if (StringUtils.isBlank(vo.getLineNumbers())) {
                return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "此策略行号为空，无法回收");
            }
            ResultRO<List<DeviceRawTextsRO>> rawText = whaleDeviceObjectClient.getRawText(deviceUUID, deviceRO.getData().get(0).getRawConfigTextRefId());
            if (rawText.getData()!= null && rawText.getData().size()>0 && StringUtils.isNotBlank(rawText.getData().get(0).getRawTexts())) {
                String rawTexts = rawText.getData().get(0).getRawTexts();
                map.put("ruleText", PolicyRowUilts.getRuleText(rawTexts, vo.getLineNumbers()));
            }
        }

        try {
            RuleIPTypeEnum ruleIPTypeEnum = RuleIPTypeEnum.IP6.getName().equals(vo.getIpType())? RuleIPTypeEnum.IP6: RuleIPTypeEnum.IP4;

            StringBuffer sbString = new StringBuffer();
            boolean isVsys = deviceRO.getData().get(0).getIsVsys()==null?false:deviceRO.getData().get(0).getIsVsys();
            String vsysName = deviceRO.getData().get(0).getVsysName();
            sbString.append(overAllGeneratorAbstractBean.generatePreCommandline(isVsys,vsysName, null,null));
            sbString.append(overAllGeneratorAbstractBean.deleteSecurityPolicyByIdOrName(ruleIPTypeEnum, vo.getRuleId(),vo.getName(), map,null));
            sbString.append(overAllGeneratorAbstractBean.generatePostCommandline(null,null));

            map.put("command",sbString.toString());
            return new TotemsReturnT(map);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return new TotemsReturnT(TotemsReturnT.FAIL_CODE, "命令行生成异常");
    }
}
