package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.ExistObjectDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/23
 */
@Service
public class DeleteExcessAddressReuseNameServiceImpl implements CmdService {
    @Override
    public void modify(CmdDTO cmdDto) throws Exception {
        //整体的
        ExistObjectDTO existObjectDTO =  cmdDto.getExistObject();

        String dstAddressObjectName = existObjectDTO.getDstAddressObjectName();
        String srcAddressObjectName = existObjectDTO.getSrcAddressObjectName();
        dstAddressObjectName = deletePrefixAddressObject(dstAddressObjectName);
        existObjectDTO.setDstAddressObjectName(dstAddressObjectName);
        srcAddressObjectName = deletePrefixAddressObject(srcAddressObjectName);
        existObjectDTO.setSrcAddressObjectName(srcAddressObjectName);

        //existObjectDTO 离散复用 todo 迪普不存在 其他型号可以进行区别开发
    }

    /**
     * 去掉前缀
     * @param addressObjectName
     * @return
     */
    private String deletePrefixAddressObject ( String addressObjectName  ){
        if(StringUtils.isNotEmpty(addressObjectName)){
            Matcher matcher =  CommonConstants.IP_OBJ_PATTERN.matcher(addressObjectName);
            if(matcher.find()){
               String newAddressObjectName = matcher.group("obj1");
                return newAddressObjectName;
            }else{
                return addressObjectName;
            }
        }

        return addressObjectName;
    }
}
