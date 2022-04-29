package com.abtnetworks.totems.common.commandline.common.service.impl;

import com.abtnetworks.totems.common.commandline.common.domain.IpObjectDO;
import com.abtnetworks.totems.common.commandline.common.dto.IpInfoDTO;
import com.abtnetworks.totems.common.commandline.common.enums.IpCreateTypeEnum;
import com.abtnetworks.totems.common.commandline.common.service.AddressObjectCommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.abtnetworks.totems.common.commandline.common.enums.IpCreateTypeEnum.GROUP_OBJECT;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/4/12
 */
@Slf4j
@Service
public class AddressObjectCommonServiceImpl implements AddressObjectCommonService {
    @Override
    public IpObjectDO createIpObjectName(IpInfoDTO ipInfoDTO) {
        IpObjectDO ipObjectDO = new IpObjectDO();
        if (ObjectUtils.isNotEmpty(ipInfoDTO)) {
            IpCreateTypeEnum ipCreateTypeEnum = ipInfoDTO.getIpCreateTypeEnum();
            List<String> ips = ipInfoDTO.getIps();
            if (CollectionUtils.isNotEmpty(ips) && GROUP_OBJECT.equals(ipCreateTypeEnum)) {
                log.debug("多个ip地址对象");
                Collections.sort(ips);
                int i = ips.hashCode();
                String name = "G_" + i;
                ipObjectDO.setIpObjectName(name);
            } else {
                log.info("其他类型");
            }

        } else {
            log.error("对象名参数为空");
        }
        return ipObjectDO;
    }
}
