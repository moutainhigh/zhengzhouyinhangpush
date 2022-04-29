package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.generate.subservice.SubServiceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 地址对象复用采取先整体复用在离散复用的流程
 * @date 2020/8/12
 */
@Slf4j
@Service
public class SearchUnitaryAndDiscreteExistAddressServiceImpl  implements CmdService {
    @Autowired
    Map<String, CmdService> cmdServiceMap;
    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        log.info(String.format("高级设置复用现有服务对象，先整体复用获取，再离散服务对象......"));
        //先查整体复用情况
        CmdService serviceUnitaryFormat = cmdServiceMap.get(NameUtils.getServiceDefaultName(SubServiceEnum.SEARCH_UNITARY_EXIST_ADDRESS.getServiceClass()));
        serviceUnitaryFormat.modify(cmdDto);
        //再离散查复用
        CmdService serviceDiscreteFormat = cmdServiceMap.get(NameUtils.getServiceDefaultName(SubServiceEnum.SEARCH_DISCRETE_EXIST_ADDRESS.getServiceClass()));
        serviceDiscreteFormat.modify(cmdDto);
    }
}
