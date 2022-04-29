package com.abtnetworks.totems.commandLine.utils;

import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import com.abtnetworks.totems.commandLine.vo.IpAddressRangeVO;
import com.abtnetworks.totems.commandLine.vo.IpAddressSubnetIntVO;
import com.abtnetworks.totems.commandLine.vo.IpAddressSubnetStrVO;
import org.springframework.beans.BeanUtils;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/8/10
 */
public class ParamVOToDTOUtil {

    public static IpAddressRangeDTO ipAddressRangeVOToDTO(IpAddressRangeVO ipAddressRangeVO){
        IpAddressRangeDTO ipAddressRangeDTO = new IpAddressRangeDTO();
        BeanUtils.copyProperties(ipAddressRangeVO,ipAddressRangeDTO);
        return ipAddressRangeDTO;
    }

    public static IpAddressSubnetIntDTO ipAddressSubnetIntVOToDTO(IpAddressSubnetIntVO ipAddressSubnetIntVO){
        IpAddressSubnetIntDTO ipAddressSubnetIntDTO = new IpAddressSubnetIntDTO();
        BeanUtils.copyProperties(ipAddressSubnetIntVO,ipAddressSubnetIntDTO);
        ipAddressSubnetIntDTO.setType(MaskTypeEnum.getByCode(ipAddressSubnetIntVO.getMaskTypeEnumCode()));
        return ipAddressSubnetIntDTO;
    }

    public static IpAddressSubnetStrDTO ipAddressSubnetStrVOToDTO(IpAddressSubnetStrVO ipAddressSubnetStrVO){
        IpAddressSubnetStrDTO ipAddressSubnetStrDTO = new IpAddressSubnetStrDTO();
        BeanUtils.copyProperties(ipAddressSubnetStrVO,ipAddressSubnetStrDTO);
        ipAddressSubnetStrDTO.setType(MaskTypeEnum.getByCode(ipAddressSubnetStrVO.getMaskTypeEnumCode()));
        return ipAddressSubnetStrDTO;
    }
}
