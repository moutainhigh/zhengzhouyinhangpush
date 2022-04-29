package com.abtnetworks.totems.common.dto.commandline;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luwei
 * @date 2019/4/28
 */
@ApiModel(value = "策略对象")
@Data
public class PolicyObjectDTO {

    @ApiModelProperty("对象名称")
    String name;

    @ApiModelProperty("命令行，定义对象需要拼接多种信息")
    String commandLine;

    @ApiModelProperty("命令集合，当不适用String格式时，使用list格式")
    List<String> commandLineList;

    @ApiModelProperty("是否是对象：true时，表示是对象, name是对象名称")
    boolean isObjectFlag;

    @ApiModelProperty("衔接内容，当isObjectFlag=true时，才会衔接对象")
    String join;
    @ApiModelProperty("已经存在服务对象-离散复用时使用")
    List<String> existServiceNameList;
    @ApiModelProperty("已经存在地址对象-离散复用时使用")
    List<String> existAddressNameList;
    /**
     * 这里三个都是回滚中查询使用
     */

    String firstIpNameJoin;
    String firstServiceJoin;

    @ApiModelProperty("是否是组(用于对应回滚判断是单个还是组,单个拼接单个的命令行,组则拼接组的命令行)")
    boolean isGroup = false;

    @ApiModelProperty("回滚命令行拼接参数。针对于juniper 回滚地址对象的时候需要地址名称和实际的地址内容 ；回滚地址组的时候需要地址组名称和地址名称" +
            "回滚对象：delete security zones security-zone untrust address-book address DNS10 192.168.1.10/32 --DNS10为名称 后面为实际地址,untrust为" +
            "回滚地址组：delete security zones security-zone untrust address-book address-set DNSGROUP address DNS10 --DNSGROUP为地址组名称，后面DNS10为地址名称")
    String joinRollbackParam;

    @ApiModelProperty("ip类型  0：ipv4; 1:ipv6; 2:url ")
    private Integer ipType;

    @ApiModelProperty("地址类型map ，key为地址名称，value为地址的类型 host主机，sub子网 rang 范围")
    private Map<String,String> addressTypeMap = new HashMap<>();
    @ApiModelProperty("新建对象名称 ")
    List<String> createObjectName ;
    @ApiModelProperty("新建对象组名称 ")
    List<String> createGroupObjectName ;
    @ApiModelProperty("新建服务对象名称 ")
    List<String> createServiceObjectName = new ArrayList<>();
    @ApiModelProperty("新建服务组名称 ")
    List<String> createServiceGroupObjectNames;
}
