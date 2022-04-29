package com.abtnetworks.totems.common.commandline.common.dto;




import com.abtnetworks.totems.common.commandline.common.enums.IpCreateTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/4/12
 */
@Data
public class IpInfoDTO {

    /*****/
   private List<String> ips;
    /*****/
   private IpCreateTypeEnum ipCreateTypeEnum;


    public IpInfoDTO(List<String> ips, IpCreateTypeEnum ipCreateTypeEnum) {

        this.ips = ips;
        this.ipCreateTypeEnum = ipCreateTypeEnum;

    }
    public IpInfoDTO(){}
}
