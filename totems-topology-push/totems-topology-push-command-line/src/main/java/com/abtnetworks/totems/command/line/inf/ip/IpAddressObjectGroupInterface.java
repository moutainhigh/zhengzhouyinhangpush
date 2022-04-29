package com.abtnetworks.totems.command.line.inf.ip;

import com.abtnetworks.totems.command.line.dto.IpAddressRangeDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetIntDTO;
import com.abtnetworks.totems.command.line.dto.IpAddressSubnetStrDTO;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:23'.
 */
public interface IpAddressObjectGroupInterface extends BasicInterface {


    /**
     * 生成地址组对象名称
     * @param ipTypeEnum ip类型
     * @param groupName 地址组名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateIpAddressObjectGroupName(RuleIPTypeEnum ipTypeEnum,String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    /**
     * 删除地址组对象
     * @param delStr 删除标记字符串
     * @param groupName 地址组名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String deleteIpAddressObjectGroupCommandLine(RuleIPTypeEnum ipTypeEnum,String delStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("%s %s", delStr, groupName);
    }


    /**
     * 创建地址组对象
     * @param statusTypeEnum 状态类型
     * @param ipTypeEnum IP枚举类型
     * @param name 地址对象名称
     * @param id 地址对象id
     * @param singleIpArray 单个ip
     * @param rangIpArray 范围ip
     * @param subnetIntIpArray 子网ip 掩码int
     * @param subnetStrIpArray 子网ip 掩码str
     * @param interfaceArray 接口集合
     * @param fqdnArray 域名集合
     * @param objectNameRefArray 引用对象名称集合
     * @param objectGroupNameRefArray 引用对象组名称集合
     * @param description 备注
     * @param attachStr 附加字符串
     * @param delStr 删除，失效标记
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                           String name, String id,
                                                           String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                           IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                           String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                           String description, String attachStr, String delStr,
                                                           Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 排除 地址组对象的内容 与 generateIpAddressObjectGroupCommandLine 组合使用
     * @param statusTypeEnum 状态类型
     * @param ipTypeEnum IP枚举类型
     * @param singleIpArray 单个ip
     * @param rangIpArray 范围ip
     * @param subnetIntIpArray 子网ip 掩码int
     * @param subnetStrIpArray 子网ip 掩码str
     * @param interfaceArray 接口集合
     * @param fqdnArray 域名集合
     * @param objectNameRefArray 引用对象名称集合
     * @param objectGroupNameRefArray 引用对象组名称集合
     * @param description 备注
     * @param attachStr 附加字符串
     * @param delStr 删除，失效标记
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateExcludeIpAddressObjectGroupCommandLine(StatusTypeEnum statusTypeEnum, RuleIPTypeEnum ipTypeEnum,
                                                                  String[] singleIpArray, IpAddressRangeDTO[] rangIpArray,
                                                                  IpAddressSubnetIntDTO[] subnetIntIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                                  String[] interfaceArray, String[] fqdnArray, String[] objectNameRefArray, String[] objectGroupNameRefArray,
                                                                  String description, String attachStr, String delStr,
                                                                  Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

    /**
     * 生成地址管理地址组命令行
     * @param statusTypeEnum 状态类型
     * @param name 地址组名称
     * @param singleIpArray 单个ip
     * @param rangIpArray 范围ip
     * @param subnetStrIpArray 子网ip 掩码str
     * @param fqdnArray 域名集合
     * @param objectNameArray 对象名称集合
     * @param addObjectNameArray 新增对象名称集合
     * @param delObjectNameArray 删除对象名称集合
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateManageIpAddressGroupObjectCommandLine(StatusTypeEnum statusTypeEnum, String name, String[] singleIpArray,
                                                                 IpAddressRangeDTO[] rangIpArray, IpAddressSubnetStrDTO[] subnetStrIpArray,
                                                                 String[] fqdnArray, String[] objectNameArray, String[] addObjectNameArray, String[] delObjectNameArray, Map<String, Object> map, String[] args) {
        return "";
    }
}
