package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.dto.generate.ExistAddressObjectDTO;
import com.abtnetworks.totems.common.enums.AddressPropertyEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.abtnetworks.totems.common.enums.AddressPropertyEnum.DST;
import static com.abtnetworks.totems.common.enums.AddressPropertyEnum.POST_DST;
import static com.abtnetworks.totems.common.enums.AddressPropertyEnum.POST_SRC;
import static com.abtnetworks.totems.common.enums.AddressPropertyEnum.SRC;

/**
 * @Description 该类用于整体查找当前已存在地址对象
 *              整体查找的意思是将输入的所有的IP地址同时作为查找参数一次性查找。
 *              对于单个IP地址，有可能是地址对象或者对象组，对于多个IP地址，这是地址对象组。
 *              本查找包括内部预定义对象。
 *              本查找为精确查找，即只有当查找出来的对象/对象组和输入的IP完全一致才行
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class SearchUnitaryEixtAddressObjectCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;


    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO deviceDTO = cmdDTO.getDevice();

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        ExistObjectDTO existObjectDTO = cmdDTO.getExistObject();

        SettingDTO settingDTO = cmdDTO.getSetting();


        if(settingDTO.isEnableAddressObjectSearch()) {
            log.info("进行整体地址对象/对象组查询...");
            //兼容新对象到命令行中
            ExistAddressObjectDTO existSrcAddressObjectDTO = getCurrentAddressObjectName(deviceDTO, policyDTO,SRC,settingDTO);
            if(existSrcAddressObjectDTO != null){
                ArrayList arrayList = new ArrayList();
                arrayList.add(existSrcAddressObjectDTO);
                existObjectDTO.setSrcAddressObjectName(existSrcAddressObjectDTO.getExistName());
                existObjectDTO.setExistSrcAddressName(arrayList);
            }


            if (!PolicyConstants.POLICY_DST_IS_DOMAIN.equals(policyDTO.getPolicySource())) {
                ExistAddressObjectDTO existDstAddressObjectDTO = getCurrentAddressObjectName(deviceDTO, policyDTO,DST,settingDTO);
                if(existDstAddressObjectDTO != null){
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(existDstAddressObjectDTO);
                    existObjectDTO.setDstAddressObjectName(existDstAddressObjectDTO.getExistName());
                    existObjectDTO.setExistDstAddressName(arrayList);
                }
            }
            ExistAddressObjectDTO existPostSrcAddressObjectDTO =  getCurrentAddressObjectName(deviceDTO, policyDTO,POST_SRC,settingDTO);
            if(existPostSrcAddressObjectDTO != null){
                ArrayList arrayList = new ArrayList();
                arrayList.add(existPostSrcAddressObjectDTO);
                existObjectDTO.setPostSrcAddressObjectName(existPostSrcAddressObjectDTO.getExistName());
                existObjectDTO.setExistPostSrcAddressName(arrayList);
            }
            ExistAddressObjectDTO existPostDstAddressObjectDTO = getCurrentAddressObjectName(deviceDTO,policyDTO,POST_DST,settingDTO);
            if(existPostDstAddressObjectDTO != null){
                ArrayList arrayList = new ArrayList();
                arrayList.add(existPostDstAddressObjectDTO);
                existObjectDTO.setPostDstAddressObjectName(existPostDstAddressObjectDTO.getExistName());
                existObjectDTO.setExistPostSrcAddressName(arrayList);
            }

        } else {
            log.info("不进行整体地址对象/对象组查询...");
        }

    }

    /**
     * 获取设备已存在地址对象名称
     * @param deviceDTO 设备UUID
     * @param policyDTO 地址
     * @return 已存在地址对象名称
     */
    private ExistAddressObjectDTO getCurrentAddressObjectName(DeviceDTO deviceDTO, PolicyDTO policyDTO , AddressPropertyEnum addressPropertyEnum,SettingDTO settingDTO) {
        String ipAddresses;
        switch (addressPropertyEnum){
            case SRC:
                ipAddresses = policyDTO.getSrcIp();
                break;
            case DST:
                ipAddresses = policyDTO.getDstIp();
                break;
            case POST_SRC:
                ipAddresses = policyDTO.getPostSrcIp();
                break;
            case POST_DST:
                ipAddresses =  policyDTO.getPostDstIp();
                break;
            default:
                ipAddresses = "";
                break;
        }
        if(AliStringUtils.isEmpty(ipAddresses)) {
            log.info("查询现有地址对象，地址为空，不进行查询...");
            return null;
        }
        log.info(String.format("设备%s，地址为%s", deviceDTO.getDeviceUuid(), ipAddresses));
        ExistAddressObjectDTO existAddressObjectDTO = whaleManager.getCurrentAddressObjectName(ipAddresses, deviceDTO,addressPropertyEnum, policyDTO.getType(),settingDTO, policyDTO);
        return existAddressObjectDTO;
    }
}
