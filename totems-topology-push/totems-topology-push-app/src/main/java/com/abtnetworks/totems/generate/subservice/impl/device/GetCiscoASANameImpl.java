package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.common.dto.*;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.TotemsIp4Utils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author lb
 * @desc 获取CiscoASA名称 能否被复用
 * @date 2021/12/2 16:37
 */
@Slf4j
@Service
public class GetCiscoASANameImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        DeviceDTO device = cmdDto.getDevice();
        PolicyDTO policy = cmdDto.getPolicy();
        SettingDTO setting = cmdDto.getSetting();
        String srcZone = policy.getSrcZone();
        String dstZone = policy.getDstZone();
        String postSrcIp = policy.getPostSrcIp();
        String deviceUuid = device.getDeviceUuid();
        ResultRO<List<DeviceFilterlistRO>> dataResultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
            log.info("没有查询到策略集");
            return;
        }
        List<DeviceFilterlistRO> resultList = new ArrayList<>();
        String typeStr = "SYSTEM__NAT_LIST";
        for (DeviceFilterlistRO ro : dataResultRO.getData()) {
            String ruleListType = ro.getRuleListType();
            if (StringUtils.isBlank(ruleListType)) {
                continue;
            }
            //其他策略集
            boolean booleanType = ruleListType.contains(typeStr);
            if (booleanType) {
                resultList.add(ro);
            }

            if (CollectionUtils.isNotEmpty(resultList)){
                List<String> uuids = resultList.stream().map(p -> p.getUuid()).collect(Collectors.toList());
                for (String uuid : uuids) {
                    ResultRO<List<JSONObject>> dataResultRO1 = whaleDevicePolicyClient.getRuleIndex(deviceUuid,uuid);
                    if (dataResultRO1 == null || !dataResultRO1.getSuccess() || dataResultRO1.getData() == null || dataResultRO1.getData().size() == 0) {
                        log.info("没有查询到策略集的策略");
                        return;
                    }
                    for (JSONObject data : dataResultRO1.getData()) {
                        boolean isSrcZone = true;
                        boolean useEifAsPostSrcIp = false;

                        if (data.containsKey("miscFields")){
                            if (StringUtils.isNotEmpty(data.get("miscFields").toString())){
                                if (JSONObject.parseObject(data.get("miscFields").toString()).containsKey("useEifAsPostSrcIp") &&
                                        JSONObject.parseObject(data.get("miscFields").toString()).getBoolean("useEifAsPostSrcIp")){
                                    useEifAsPostSrcIp = true;
                                }
                            }
                        }

                        if (StringUtils.isNotEmpty(srcZone) && data.containsKey("srcZone")){
                            if (!srcZone.equals(data.get("srcZone"))){
                                isSrcZone = false;
                            }
                        }else if (StringUtils.isNotEmpty(srcZone) || data.containsKey("srcZone")){
                            isSrcZone = false;
                        }

                        if (StringUtils.isNotEmpty(dstZone) && data.containsKey("dstZone")){
                            if (!dstZone.equals(data.get("dstZone"))){
                                continue;
                            }
                        }else if (StringUtils.isNotEmpty(dstZone) || data.containsKey("dstZone")){
                            continue;
                        }
                        JSONArray jsonArray = null;
                        if (data.containsKey("postTrafficElementList")){
                            jsonArray = JSONArray.parseArray(JSONObject.toJSONString(data.get("postTrafficElementList")));
                        }
                        boolean postSrc = true;
                        if (StringUtils.isNotEmpty(postSrcIp)){
                            if (jsonArray != null && jsonArray.size() > 0){
                                for (int i =0 ;i < jsonArray.size();i++){
                                    if (JSON.parseObject(jsonArray.get(i).toString()).containsKey("ip4SrcAddresses")){
                                        List<IpAddressDTO> ip4SrcAddresses = JSONArray.parseArray(JSON.parseObject(jsonArray.get(i).toString()).get("ip4SrcAddresses").toString()).toJavaList(IpAddressDTO.class);
                                        String[] addresses = postSrcIp.split(",");
                                        int length = addresses.length;
                                        for (int x =0 ;x < ip4SrcAddresses.size();x++){
                                            for (int y =0 ;y < length;y++){
                                                if ( x == y){
                                                    IpAddressDTO ipAddressDTO = ip4SrcAddresses.get(x);
                                                    Pair<String, String> ipStartEndString = TotemsIp4Utils.getIpStartEndString(addresses[y]);
                                                    if (!ipAddressDTO.getStart().equals(ipStartEndString.getLeft()) ||
                                                            !ipAddressDTO.getEnd().equals(ipStartEndString.getRight())){
                                                        postSrc = false;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }else {
                                postSrc = false;
                            }

                    }else {
                            if (jsonArray != null && jsonArray.size() > 0){
                                for (int i =0 ;i < jsonArray.size();i++){
                                    if (JSON.parseObject(jsonArray.get(i).toString()).containsKey("ip4SrcAddresses")){
                                        postSrc = false;
                                    }
                                }
                            }
                        }

                        if(postSrc){
                            if (data.containsKey("natFilterRuleListRef") && isSrcZone){
                                String natFilterRuleListRef = data.getString("natFilterRuleListRef");
                                policy.setExistAclName(natFilterRuleListRef);
                                break;
                            }
                        }
                        if ((useEifAsPostSrcIp && StringUtils.isEmpty(postSrcIp)) || (postSrc && isSrcZone)){
                            setting.setPolicyId(data.get("ruleId").toString());
                            policy.setExistGlobal(true);
                            break;
                        }


                    }
                }
            }

        }

    }
}
