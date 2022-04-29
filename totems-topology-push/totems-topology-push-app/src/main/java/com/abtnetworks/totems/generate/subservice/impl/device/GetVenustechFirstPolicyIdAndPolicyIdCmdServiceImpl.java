package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.generate.subservice.impl.GetFirstPolicyNameInZoneCmdServiceImpl;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description 该类用于获取启明星辰设备接口上现有策略集中第一条策略的ID以及当前生成策略所使用的ID。
 *              启明星辰的策略id需要在域间移动，例如：若策略的源域和目的域分别为trust和untrust，则
 *              生成的策略只有移动到trust到untrust的所有策略的第一条才行，因此其移动对象也是trust
 *              到untrust的第一条策略的id，而非所有策略集的第一条，因此其查找方式与飞塔有区别，所以
 *              另外写一套微服务。
 *              其获取当前策略id的方式与飞塔也有不同，是插空选取没有使用的最大id
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetVenustechFirstPolicyIdAndPolicyIdCmdServiceImpl extends GetFirstPolicyNameInZoneCmdServiceImpl {

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        log.debug("Venustech VSOS的策略id得根据已有的策略id来累加,移动策略的获得同一个策略集的第一条策略");

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();

        PolicyDTO policyDTO = cmdDTO.getPolicy();
        String srcZone = policyDTO.getSrcZone();
        String dstZone = policyDTO.getDstZone();

        String ruleListUuid = deviceDTO.getRuleListUuid();

        SettingDTO settingDTO = cmdDTO.getSetting();
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = getPolicyRuleList(deviceUuid, ruleListUuid);
        //KSH-5469
        int currentId = advancedSettingService.getFortinetPolicyId(deviceUuid);
        Integer freeId ;
        if(ObjectUtils.isNotEmpty(resultRO)) {
            synchronized (GetVenustechFirstPolicyIdAndPolicyIdCmdServiceImpl.class) {

                freeId = getFreePolicyId(resultRO, currentId + 1);
                String firstPolicyId = getFirstPolicyIdInterZone(resultRO, srcZone, dstZone);
                if (firstPolicyId != null) {
                    if (firstPolicyId.startsWith("Default")) {
                        firstPolicyId = null;
                    }
                }
                settingDTO.setPolicyId(String.valueOf(freeId));
                settingDTO.setSwapNameId(firstPolicyId);
            }
        }else{
            freeId = currentId + 1;
            settingDTO.setPolicyId(String.valueOf(freeId));
            log.info("启明星辰防火墙没有策略集及安全测录");
        }
        advancedSettingService.setFortinetPolicyId(deviceUuid, freeId);
    }

    protected ResultRO<List<DeviceFilterRuleListRO>> getPolicyRuleList(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        log.debug(String.format("启明星辰(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSONObject.toJSONString(resultRO, SerializerFeature.PrettyFormat) + "\n-----------------------------------\n");
        return resultRO;
    }

    /**
     * 获取域间第一条策略的id
     * @param resultRO 策略集数据
     * @param srcZone 源域
     * @param dstZone 目的域
     * @return 域间第一条策略的id
     */
    protected String getFirstPolicyIdInterZone(ResultRO<List<DeviceFilterRuleListRO>> resultRO, String srcZone, String dstZone) {
        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if (list == null || list.isEmpty()) {
            log.error("获取域间第一条策略时，策略集合为空");
            return name;
        }
        for (DeviceFilterRuleListRO ruleListRO : list) {
            List<String> inInterfaceGroupRefs = ruleListRO.getInInterfaceGroupRefs();
            List<String> outInterfaceGroupRefs = ruleListRO.getOutInterfaceGroupRefs();
            //2个域都 非空
            if (StringUtils.isNoneBlank(srcZone, dstZone) && inInterfaceGroupRefs != null && outInterfaceGroupRefs != null
                    && inInterfaceGroupRefs.contains(srcZone) && outInterfaceGroupRefs.contains(dstZone)) {
                name = ruleListRO.getRuleId();
                break;
            }
            //2个域都 空
            if (StringUtils.isAllBlank(srcZone, dstZone)
                    && (inInterfaceGroupRefs == null || inInterfaceGroupRefs.isEmpty())
                    && (outInterfaceGroupRefs == null || outInterfaceGroupRefs.isEmpty())) {
                name = ruleListRO.getRuleId();
                break;
            }
            //源域非空 ， 目的域空
            if (StringUtils.isNotBlank(srcZone) && StringUtils.isBlank(dstZone)
                    && inInterfaceGroupRefs != null && !inInterfaceGroupRefs.isEmpty()
                    && inInterfaceGroupRefs.contains(srcZone)
                    && (outInterfaceGroupRefs == null || outInterfaceGroupRefs.isEmpty())) {
                name = ruleListRO.getRuleId();
                break;
            }

            //源域空  ，  目的域非空
            if (StringUtils.isBlank(srcZone) && StringUtils.isNotBlank(dstZone)
                    && (inInterfaceGroupRefs == null || inInterfaceGroupRefs.isEmpty())
                    && outInterfaceGroupRefs != null && !outInterfaceGroupRefs.isEmpty()
                    && outInterfaceGroupRefs.contains(dstZone)) {
                name = ruleListRO.getRuleId();
                break;
            }

        }

        return name;
    }

    /**
     * 获取没有使用的最小策略id
     *
     * @param resultRO 策略集数据对象
     * @param id 可使用的最小id
     * @return 比当前已使用id大的最小的策略集中未使用的id
     */
    protected Integer getFreePolicyId(ResultRO<List<DeviceFilterRuleListRO>> resultRO, Integer id) {
        List<DeviceFilterRuleListRO> list = resultRO.getData();

        Set<Integer> idSet = new HashSet<>();
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO rule: resultRO.getData()) {
                Integer ruleId = 0;
                try{
                    String ruleId1 = rule.getRuleId();
                    if(StringUtils.isBlank(ruleId1) && !StringUtils.isNumeric(ruleId1)){
                        continue;
                    }
                    ruleId = Integer.valueOf(ruleId1);
                } catch(Exception e) {
                    log.error(String.format("启明行程策略 %s ID异常", JSONObject.toJSONString(rule, SerializerFeature.PrettyFormat)));
                }
                idSet.add(ruleId);
            }
        }

        while(idSet.contains(id)) {
            id++;
        }

        return id;
    }
}
