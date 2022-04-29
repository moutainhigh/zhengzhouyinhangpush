package com.abtnetworks.totems.common.commandline.common.service;

import com.abtnetworks.totems.common.commandline.common.domain.IpObjectDO;
import com.abtnetworks.totems.common.commandline.common.dto.IpInfoDTO;

/**
 * @author Administrator
 * @Title:
 * @Description: 地址对象公共的方法
 *
 * @date 2021/4/12
 */
public interface AddressObjectCommonService {

    /**
     * 创建ip地址对象名
     * @param ipInfoDTO
     * @return
     */
    IpObjectDO createIpObjectName(IpInfoDTO ipInfoDTO);
}
