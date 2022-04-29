package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 该类用于离散查找当前已存在地址对象
 *              离散查找的意思是将输入的所有的IP地址拆分为单个IP以后，分别作为IP地址对象进行查找。
 *              例如对于IP地址串：192.168.11.1,192.168.11.10-192.168.11.11,192.168.11.174/28
 *              则分别对"192.168.11.1","192.168.11.10-192.168.11.11"和"192.168.11.174/28”
 *              进行地址对象查询。若查询到则保存到已存在地址对象列表中。若为查询到，则保存在其他地址列表中
 *              本查找包括内部预定义对象。
 *              本查找为精确查找，即只有当查找出来的对象/对象组和输入的IP完全一致才行
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class SearchDiscreteExistAddressCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO deviceDTO = cmdDTO.getDevice();

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();

        SettingDTO settingDTO = cmdDTO.getSetting();

        log.info("离散地址对象拆分...");
        List<String> existSrcAddressList = new ArrayList();
        List<String> restSrcAddressList = new ArrayList<>();
        List<ExistAddressObjectDTO> existSrcAddressName = getExistAddressName(policyDTO.getSrcIp(), deviceDTO, existSrcAddressList, restSrcAddressList, settingDTO.isCreateObject(),AddressPropertyEnum.SRC,settingDTO,policyDTO);
        existObjectDTO.getExistSrcAddressList().addAll(existSrcAddressList);
        existObjectDTO.getRestSrcAddressList().addAll(restSrcAddressList);
        existObjectDTO.setExistSrcAddressName(existSrcAddressName);
        if (CollectionUtils.isNotEmpty(restSrcAddressList) && StringUtils.isNotBlank(policyDTO.getSrcIpSystem())) {
            boolean ipSystemExist = whaleManager.queryIpSystemHasExist(deviceDTO.getDeviceUuid(), policyDTO.getSrcIpSystem());
            if (ipSystemExist) {
                policyDTO.setSrcIpSystem(String.format("%s_%s", policyDTO.getSrcIpSystem(), IdGen.getRandomNumberString()));
            }
        }

        List<String> existDstAddressList = new ArrayList();
        List<String> restDstAddressList = new ArrayList<>();
        List<ExistAddressObjectDTO> existDstAddressName = getExistAddressName(policyDTO.getDstIp(), deviceDTO, existDstAddressList, restDstAddressList, settingDTO.isEnableAddressObjectSearch(),AddressPropertyEnum.DST,settingDTO,policyDTO);
        existObjectDTO.getExistDstAddressList().addAll(existDstAddressList);
        existObjectDTO.getRestDstAddressList().addAll(restDstAddressList);
        existObjectDTO.setExistDstAddressName(existDstAddressName);
        if (CollectionUtils.isNotEmpty(restDstAddressList) && StringUtils.isNotBlank(policyDTO.getDstIpSystem())) {
            boolean ipSystemExist = whaleManager.queryIpSystemHasExist(deviceDTO.getDeviceUuid(), policyDTO.getDstIpSystem());
            if (ipSystemExist) {
                policyDTO.setDstIpSystem(String.format("%s_%s", policyDTO.getDstIpSystem(), IdGen.getRandomNumberString()));
            }
        }

        List<String> existPostSrcAddressList = new ArrayList();
        List<String> restPostSrcAddressList = new ArrayList<>();
        List<ExistAddressObjectDTO> existPostSrcAddressName = getExistAddressName(policyDTO.getPostSrcIp(), deviceDTO, existPostSrcAddressList, restPostSrcAddressList, settingDTO.isEnableAddressObjectSearch(),AddressPropertyEnum.POST_SRC,settingDTO,policyDTO);
        existObjectDTO.getExistPostSrcAddressList().addAll(existPostSrcAddressList);
        existObjectDTO.getRestPostSrcAddressList().addAll(restPostSrcAddressList);
        existObjectDTO.setExistPostSrcAddressName(existPostSrcAddressName);
        if (CollectionUtils.isNotEmpty(restPostSrcAddressList) && StringUtils.isNotBlank(policyDTO.getPostSrcIpSystem())) {
            boolean ipSystemExist = whaleManager.queryIpSystemHasExist(deviceDTO.getDeviceUuid(), policyDTO.getPostSrcIpSystem());
            if (ipSystemExist) {
                policyDTO.setPostSrcIpSystem(String.format("%s_%s", policyDTO.getPostSrcIpSystem(), IdGen.getRandomNumberString()));
            }
        }

        List<String> existPostDstAddressList = new ArrayList();
        List<String> restPostDstSrcAddressList = new ArrayList<>();
        List<ExistAddressObjectDTO> existPostDstAddressName = getExistAddressName(policyDTO.getPostDstIp(), deviceDTO, existPostDstAddressList, restPostDstSrcAddressList, settingDTO.isEnableAddressObjectSearch(),AddressPropertyEnum.POST_DST,settingDTO,policyDTO);
        existObjectDTO.getExistPostDstAddressList().addAll(existPostDstAddressList);
        existObjectDTO.getRestPostDstAddressList().addAll(restPostDstSrcAddressList);
        existObjectDTO.setExistPostDstAddressName(existPostDstAddressName);
        if (CollectionUtils.isNotEmpty(restPostDstSrcAddressList) && StringUtils.isNotBlank(policyDTO.getPostDstIpSystem())) {
            boolean ipSystemExist = whaleManager.queryIpSystemHasExist(deviceDTO.getDeviceUuid(), policyDTO.getPostDstIpSystem());
            if (ipSystemExist) {
                policyDTO.setPostDstIpSystem(String.format("%s_%s", policyDTO.getPostDstIpSystem(), IdGen.getRandomNumberString()));
            }
        }

        log.info("离散地址查找之后cmdDTO is {}", JSONObject.toJSONString(cmdDTO, true));
    }

    /**
     * 将地址对象字符串转换成离散的IP/IP范围/IP子网列表，并可对每个单独的IP/IP范围/IP子网进行地址对象复用
     * @param ipAddresses 地址字符串，支持多个IP/IP范围/IP子网，用逗号分隔
     * @param deviceDTO 设备UUID
     * @param existAddressList 已存在地址对象列表, 对于要进行地址对象复用的情况，查找到的地址对象名称保存到此列表中
     * @param restAddressList 需要生成地址对象列表
     * @param isReuse 是否复用对象，true为进行地址对象查询，false为不进行地址对象查询
     */
    private List<ExistAddressObjectDTO> getExistAddressName(String ipAddresses, DeviceDTO deviceDTO, List<String> existAddressList,
                                                            List<String> restAddressList, boolean isReuse, AddressPropertyEnum addressPropertyEnum, SettingDTO settingDTO, PolicyDTO policyDTO) {
        log.info("编号--DSTADDRESSNAME00002，当前命令行生成的目的地址为：：" + ipAddresses);
        if(AliStringUtils.isEmpty(ipAddresses)) {
            return null;
        }
        String[] ipList = ipAddresses.split(",");

        log.info("离散地址列表为：" + JSONObject.toJSONString(ipList));
        List<ExistAddressObjectDTO> existAddressName = new ArrayList<>();
        for(String ip: ipList) {
            log.info("编号--DSTADDRESSNAME00003，当前命令行生成的目的地址为：：" + ip);
            if(isReuse && !isDomainForIp(ip)) {
                ExistAddressObjectDTO addressName = whaleManager.getCurrentAddressObjectName(ip, deviceDTO,addressPropertyEnum, null,settingDTO,policyDTO);
                if(addressName == null) {
                    restAddressList.add(ip);
                } else {
                    if(ObjectUtils.isNotEmpty(addressName)){
                        existAddressList.add(addressName.getExistName());
                        existAddressName.add(addressName);
                    }

                }
            } else {
                restAddressList.add(ip);
            }
        }

        log.info("已查询到地址对象列表为：" + JSONObject.toJSONString(existAddressList));
        log.info("未查询到地址对象列表为：" + JSONObject.toJSON(restAddressList));
        return existAddressName;
    }

    /**
     * 判断目的IP是否是域名
     * @param dstIp
     * @return
     */
    private Boolean isDomainForIp(String dstIp) {
        if (IpUtils.isIPSegment(dstIp) || IpUtils.isIPRange(dstIp) || IpUtils.isIPv6(dstIp) || IpUtils.isIP(dstIp)) {
            return false;
        } else {
            return true;
        }
    }
}
