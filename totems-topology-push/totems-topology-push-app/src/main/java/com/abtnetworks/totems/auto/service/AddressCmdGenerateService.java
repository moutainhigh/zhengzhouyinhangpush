package com.abtnetworks.totems.auto.service;

import com.abtnetworks.totems.auto.vo.AddressManageDetailVO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;

import java.util.List;
import java.util.Map;

/**
 * @Description 对象管理命令行生成类
 * @Version --
 * @Created by zhoumuhua on '2021-07-27 14:44:39'.
 */
public interface AddressCmdGenerateService {

    /**
     * 比较地址对象生成命令行
     * @param allAddrMap 存放设备所有地址与内容的map
     * @param sb 存放拼接命令行的StringBuilder对象
     * @param detailVO 地址详情对象
     * @param addressAddList 比较设备与基线数据后新增值
     * @param addressDelList 比较设备与基线数据后删除值
     * @param deviceModelNumberEnum 设备类型
     */
    void generateCmdCompleteCompare(Map<String, List<String>> allAddrMap, StringBuilder sb, AddressManageDetailVO detailVO, List<String> addressAddList, List<String> addressDelList, DeviceModelNumberEnum deviceModelNumberEnum, List<String> hasAdressList);

    /**
     * 获取设备命令行
     * @param deviceModelNumberEnum 设备类型
     * @param sb 存放拼接命令行的StringBuilder对象
     * @param addressName 地址名称
     * @param isAdd true为新增对象 false为删除对象
     */
    void getDeviceCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb, String addressName, String addressEntry, Boolean isAdd);

    /**
     * 获取设备前置命令行
     * @param deviceModelNumberEnum 设备类型
     * @param sb 存放拼接命令行的StringBuilder对象
     */
    void getPreCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb, Boolean isVsys, String vsysName);

    /**
     * 获取设备后置命令行
     * @param deviceModelNumberEnum 设备类型
     * @param sb 存放拼接命令行的StringBuilder对象
     */
    void getPostCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb);

    /**
     * 将地址对象加到地址组
     * @param deviceModelNumberEnum 设备类型
     * @param sb 存放拼接命令行的StringBuilder对象
     * @param addressName 地址名称
     * @param childList 当前地址的子级对象名称
     * @param addressAddList 当前地址的新增子级对象名称
     * @param addressDelList 当前地址的删除子级对象名称
     */
    void addAddressToGroupCmd(DeviceModelNumberEnum deviceModelNumberEnum, StringBuilder sb, String addressName, List<String> childList, List<String> addressAddList, List<String> addressDelList);

}
