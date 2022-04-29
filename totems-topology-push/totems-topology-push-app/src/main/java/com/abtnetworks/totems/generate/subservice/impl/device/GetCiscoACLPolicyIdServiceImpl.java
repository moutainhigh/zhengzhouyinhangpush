package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lifei
 * @desc 该类用于获取思科路由交换设备接口上策略id
 * <p>
 * 例如：1、从现有ACL策略集中 匹配到得deny策略前面得所有策略得集合。当前匹配到的deny策略的id为20,
 * 那就去mongodb查询该策略集下面策略id小于20的数据集合（set集合 有序） 例如：Set<Integer> targetSet。
 * 2、查询mysql中该设备该策略集下面的最近一次使用的策略id是多少(没有就为0)，用获取到的这个id
 * 去+1 例如：Integer newRuleId = srcRuleId + 1。
 * 3、循环将 newRuleId去和targetSet去匹配，如果匹配上了就id++ 直到匹配不上为止,最后匹配不上就返回这个id
 * eg:最后返回的这个id 要小于策略建议返回的deny策略的ruleId(这里指的就是20)，如果小于则正常进行，如果找到的id最后大于等于20
 * 则提示,"生成命令行失败,没有可用的rule id"
 * 4、将这个id更新到mysql的表中，如果mysql表没有这个记录，就新增这个记录。
 * @date 2021/4/17 14:29
 */
@Slf4j
@Service
public class GetCiscoACLPolicyIdServiceImpl implements CmdService {

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    public WhaleManager whaleService;

    private static final String splitMask = ",";

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        log.info("开始获取思科ACL策略id逻辑,入参为:{}", JSON.toJSONString(cmdDto));

        DeviceDTO deviceDTO = cmdDto.getDevice();
        TaskDTO task = cmdDto.getTask();
        String deviceUuid = deviceDTO.getDeviceUuid();

        String ruleListUuid = deviceDTO.getRuleListUuid();
        String matchRuleId = deviceDTO.getMatchRuleId();


        SettingDTO settingDTO = cmdDto.getSetting();
        // 1.统计需要生成策略的条数
        Integer policyNums = calculationPolicyNums(cmdDto);

        CommandLineBusinessInfoDTO  businessInfoDTO = new CommandLineBusinessInfoDTO();
        businessInfoDTO.setPolicyNums(policyNums);
        cmdDto.setBusinessInfoDTO(businessInfoDTO);

//        if (StringUtils.isBlank(ruleListUuid)) {
//            log.info("工单:{} 策略集uuid为空,去新建策略集并挂载在对应的接口", task.getTheme());
//            getActualUserRuleListAndSave(cmdDto, ruleListUuid, settingDTO, 0, policyNums,cmdDto.getTask().getTaskId());
//            return;
//        }
//
//        // 如果是匹配到的ruleId为空 则证明是默认策略,默认策略是手动写入的没有ruleId
//        boolean defaultPolicy = false;
//        if (StringUtils.isBlank(matchRuleId)) {
//            defaultPolicy = true;
//        }
//
//
//        // 查询策略集已经存在的ACL策略
//        log.info("根据设备id:{}策略集uuid:{}查询策略集下面策略集合", deviceUuid, ruleListUuid);
//        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleService.getFilterRuleList(deviceUuid, ruleListUuid);
//
//        log.info("查询策略集合结果为:{}", JSON.toJSONString(resultRO));
//        synchronized (GetCiscoACLPolicyIdServiceImpl.class) {
//
//            log.info("工单:{}需要生成的策略条数为:{}", task.getTheme(), policyNums);
//            // 1.查询设备中已经存在的ruleId
//            List<DeviceFilterRuleListRO> ruleListRos = resultRO.getData();
//            List<Integer> mongoRuleIds = new ArrayList<>();
//            if (!CollectionUtils.isEmpty(ruleListRos)) {
//                for (DeviceFilterRuleListRO deviceFilterRuleListRO : ruleListRos) {
//                    if (null != deviceFilterRuleListRO && StringUtils.isNotBlank(deviceFilterRuleListRO.getRuleId())) {
//                        mongoRuleIds.add(Integer.valueOf(deviceFilterRuleListRO.getRuleId()));
//                    }
//                }
//            }
//            Collections.sort(mongoRuleIds); // 升序排列
//            // 2.查询mysql 已经使用过的ruleId
//            List<Integer> usedRuleId = advancedSettingService.getCiscoRoutePolicyId(ruleListUuid);
//            Collections.sort(usedRuleId); // 升序排列
//            // 3.如果匹配的是默认deny策略,是没有匹配的ruleId过来的,这个时候生成的策略都在最后一条的后面ruleId的取值逻辑是已经存在的ruleId+10
//            if (defaultPolicy) {
//                dealDefaultPolicyUserList(cmdDto, ruleListUuid, settingDTO, policyNums, mongoRuleIds, usedRuleId);
//                return;
//            }
//            // 4. 匹配的非默认策略
//            List<Integer> dealRuleIds = dealRuleId(matchRuleId, mongoRuleIds);
//            List<Integer> freeIds = getFreePolicyId(dealRuleIds, usedRuleId, matchRuleId);
//            log.info("对比mysql和mongo最终可用策略id集合为:{}", freeIds);
//
//            List<Integer> actualUserRuleId = new ArrayList<>();
//            compare(policyNums, cmdDto, freeIds, actualUserRuleId);
//            Collections.sort(actualUserRuleId); // 升序排列
//            log.info("最终使用的策略id集合为:{}", actualUserRuleId);
//            settingDTO.setUsableRuleList(actualUserRuleId);
//            advancedSettingService.setCiscoRoutePolicyId(ruleListUuid, actualUserRuleId,task.getTaskId());
//        }
    }

    private void dealDefaultPolicyUserList(CmdDTO cmdDto, String ruleListUuid, SettingDTO settingDTO, Integer policyNums, List<Integer> mongoRuleIds, List<Integer> usedRuleId) {
        // 如果mysql里面存在的数据不为空 则取mysql最后一条去+10处理
        if (!CollectionUtils.isEmpty(usedRuleId) && !CollectionUtils.isEmpty(mongoRuleIds)) {
            //取 id大的去取
            Integer usedRuleIdInt = usedRuleId.get(usedRuleId.size() - 1);
            Integer mongoRuleIdInt = mongoRuleIds.get(mongoRuleIds.size() - 1);
            if (usedRuleIdInt > mongoRuleIdInt) {
                getActualUserRuleListAndSave(cmdDto, ruleListUuid, settingDTO, usedRuleIdInt, policyNums,cmdDto.getTask().getTaskId());
            } else {
                getActualUserRuleListAndSave(cmdDto, ruleListUuid, settingDTO, mongoRuleIdInt, policyNums,cmdDto.getTask().getTaskId());
            }
        } else if (CollectionUtils.isEmpty(usedRuleId) && CollectionUtils.isEmpty(mongoRuleIds)) {
            getActualUserRuleListAndSave(cmdDto, ruleListUuid, settingDTO, 0, policyNums,cmdDto.getTask().getTaskId());
        } else if (CollectionUtils.isEmpty(mongoRuleIds) && !CollectionUtils.isEmpty(usedRuleId)) {
            //如果mysql为空，则取mongo里面的数据最后一条去进行+10处理
            Integer usedRuleIdInt = usedRuleId.get(usedRuleId.size() - 1);
            getActualUserRuleListAndSave(cmdDto, ruleListUuid, settingDTO, usedRuleIdInt, policyNums,cmdDto.getTask().getTaskId());
        } else {
            Integer matchRuleIdInt = mongoRuleIds.get(mongoRuleIds.size() - 1);
            getActualUserRuleListAndSave(cmdDto, ruleListUuid, settingDTO, matchRuleIdInt, policyNums,cmdDto.getTask().getTaskId());
        }
    }

    /**
     * 处理mongo返回的已经使用过的ruleId 取matchRuleId之前的ruleId 保证下发的策略在deny策略前面
     *
     * @param matchRuleId
     * @param ruleIds
     * @return
     */
    private List<Integer> dealRuleId(String matchRuleId, List<Integer> ruleIds) {
        Integer matchRuleIdInt = Integer.valueOf(matchRuleId);
        List<Integer> list = new ArrayList<>();
        for (Integer ruleId : ruleIds) {
            if (ruleId <= matchRuleIdInt) {
                list.add(ruleId);
            }
        }
        return list;
    }

    /**
     * 获取实际可用的ruleList 并存库
     *
     * @param cmdDto
     * @param ruleListUuid
     * @param settingDTO
     * @param policyNums
     */
    private void getActualUserRuleListAndSave(CmdDTO cmdDto, String ruleListUuid, SettingDTO settingDTO, Integer startIndex,
                                              Integer policyNums,Integer taskId) {
        List<Integer> ruleId = getdefultRuleId(startIndex, policyNums);
        List<Integer> actualUserRuleId = new ArrayList<>();
        compare(policyNums, cmdDto, ruleId, actualUserRuleId);
        Collections.sort(actualUserRuleId); // 升序排列
        log.info("最终使用的策略id集合为:{}", actualUserRuleId);
        settingDTO.setUsableRuleList(actualUserRuleId);
        // 对于接口没有绑定acl的情况(新建策略集) 则不去存已经使用的id到mysql
        if(StringUtils.isNotBlank(ruleListUuid)){
            advancedSettingService.setCiscoRoutePolicyId(ruleListUuid, actualUserRuleId,taskId);
        }
    }

    /**
     * 获取默认的RuleI列表
     *
     * @param startIndex
     * @return
     */
    private static List<Integer> getdefultRuleId(int startIndex, int length) {
        List<Integer> ruleId = new ArrayList<>();
        int ruleIdNum = 0;
        // 最后一条已存在的策略的id为startIndex，那么新增的第一次策略的id就应该是startIndex+10，10为步长
        for (int i = startIndex + 10; ruleIdNum < length; i = i + 10) {
            ruleId.add(i);
            ruleIdNum++;
        }
        return ruleId;
    }

    /**
     * 比较需要生成的ruleId数量和可用的ruleId数量
     *
     * @param cmdDto private Integer calculationPolicyNums(CmdDTO cmdDto, List<Integer> freeIds, List<Integer> actualUserRuleId) {
     */
    private Integer calculationPolicyNums(CmdDTO cmdDto) {
            // 判断思科路由acl可用ruleId是否足够
            PolicyDTO policyDTO = cmdDto.getPolicy();
            String srcIp = policyDTO.getSrcIp();
            String dstIp = policyDTO.getDstIp();
            List<ServiceDTO> serviceList = policyDTO.getServiceList();
            Integer srcIpNum = 0;
            Integer dstIpNum = 0;
            Integer portsNum = 0;

            if (StringUtils.isBlank(srcIp)) {
                srcIpNum = srcIpNum + 1;
            } else {
                String[] arr = srcIp.split(splitMask);
                for (String itemSrcIp : arr) {
                    // 根据业务需求,如果是范围,则需要拆分成子网
                    if (IpUtils.isIPRange(itemSrcIp)) {
                        List<String> ips = IPUtil.convertRangeToSubnet(itemSrcIp);
                        srcIpNum = srcIpNum + ips.size();
                    } else {
                        srcIpNum = srcIpNum + 1;
                    }
                }
            }

            if (StringUtils.isBlank(dstIp)) {
                dstIpNum = dstIpNum + 1;
            } else {
                String[] arr = dstIp.split(splitMask);
                for (String itemDstIp : arr) {
                    // 根据业务需求,如果是范围,则需要拆分成子网
                    if (IpUtils.isIPRange(itemDstIp)) {
                        List<String> ips = IPUtil.convertRangeToSubnet(itemDstIp);
                        dstIpNum = dstIpNum + ips.size();
                    } else {
                        dstIpNum = dstIpNum + 1;
                    }
                }
            }

            for (ServiceDTO serviceDTO : serviceList) {
                String dstPorts = serviceDTO.getDstPorts();
                if (StringUtils.isBlank(dstPorts)) {
                    portsNum = portsNum + 1;
                } else {
                    String[] dstPortsStr = dstPorts.split(splitMask);
                    portsNum = portsNum + dstPortsStr.length;
                }
            }
            // 计算本次新增策略的条数
            Integer policyNums = srcIpNum * dstIpNum * portsNum;
            return policyNums;
    }

    /**
     * 对比可用的 获取实际可用的
     *
     * @param policyNums
     * @param freeIds
     * @param cmdDto
     * @param actualUserRuleId
     */
    private void compare(Integer policyNums, CmdDTO cmdDto, List<Integer> freeIds, List<Integer> actualUserRuleId) {
        // 如果生成策略所需的ruleId个数大于 可用的ruleId个数,则直接提示生成命令行失败,没有足够的ruleId可用
        if (policyNums > freeIds.size()) {
            log.info("需要生成的策略:{}的条数大于可用的ruleId个数:{},无法生成命令行", JSONObject.toJSONString(cmdDto),freeIds);
            CommandLineBusinessInfoDTO  businessInfoDTO = new CommandLineBusinessInfoDTO();
            businessInfoDTO.setRuleIdNotEnoughMsg(SendErrorEnum.NO_USABLE_RULE_ID.getMessage());
            businessInfoDTO.setPolicyNums(policyNums);
            cmdDto.setBusinessInfoDTO(businessInfoDTO);
        } else {
            // 获取实际使用的ruleIdList
            for (int i = 0; i < policyNums; i++) {
                actualUserRuleId.add(freeIds.get(i));
            }
        }
    }

    /**
     * @param ruleIds 策略集策略id集合数据对象
     * @return
     */
    protected static List<Integer> getFreePolicyId(List<Integer> ruleIds, List<Integer> ids, String matchRuleId) {

        Integer matchRuleIdInt = Integer.valueOf(matchRuleId);
        List<Integer> freeRuleId = new ArrayList<>();
        // 查询到mysql复用的id集合为空,则直接返回,可用的.
        if (null == ids || 0 == ids.size()) {
            for (int i = matchRuleIdInt; i > 0; i--) {
                if (!ruleIds.contains(i)) {
                    freeRuleId.add(i);
                }
            }
        } else {
            for (int i = matchRuleIdInt; i > 0; i--) {
                if (!ruleIds.contains(i) && !ids.contains(i)) {
                    freeRuleId.add(i);
                }
            }
        }
        return freeRuleId;
    }

}
