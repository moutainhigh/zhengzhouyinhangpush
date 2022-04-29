package com.abtnetworks.totems.push.manager;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.platform.dto.*;

import java.util.List;

/**
 * @desc
 * @author liuchanghao
 * @date 2021-02-03 13:59
 */
public interface FortiManagementPlatformManager {

    /**
     * 飞塔管理平台创建IPV4地址对象
     * @param loginDTO
     * @return
     */
    ReturnT<String> fortinetLogin(ManagementPlatformLoginDTO loginDTO);

    /**
     * 飞塔管理平台创建IPV4源地址对象
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> fortinetCreateIPV4SrcAddress(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 飞塔管理平台创建IPV4目的地址对象
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> fortinetCreateIPV4DstAddress(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 飞塔管理平台创建IPV6源地址对象
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> fortinetCreateIPV6SrcAddress(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 飞塔管理平台创建IPV6目的地址对象
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> fortinetCreateIPV6DstAddress(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 根据设备名称找到package名称
     * @param packageDTO
     * @return
     */
    ReturnT<String> getPackageNameByDeviceName(ManagementPlatformGetPackageDTO packageDTO);

    /**
     * 飞塔管理平台创建服务对象
     * @param serviceDTO
     * @return
     */
    ReturnT<List<String>> fortinetCreateServiceObject(ManagementPlatformCreateServiceDTO serviceDTO);

    /**
     * 飞塔管理平台创建时间对象
     * @param timeDTO
     * @return
     */
    ReturnT<String> fortinetCreateTimeObject(ManagementPlatformCreateTimeDTO timeDTO);

    /**
     * 飞塔管理平台生成IPV4策略
     * @param policyDTO
     * @return
     */
    ReturnT<String> fortinetCreateIPV4Policy(ManagementPlatformCreatePolicyDTO policyDTO);

    /**
     * 飞塔管理平台生成IPV4策略
     * @param policyDTO
     * @return
     */
    ReturnT<String> fortinetCreateIPV6Policy(ManagementPlatformCreatePolicyDTO policyDTO);

    /**
     * 飞塔管理平台install安装
     * @param installDTO
     * @return
     */
    ReturnT<String> fortinetInstall(ManagementPlatformInstallDTO installDTO);

    /**
     * 飞塔管理平台删除策略
     * @param
     * @return
     */
    ReturnT<String> fortinetDeletePolicy(ManagementPlatformCreatePolicyDTO policyDTO);

    /**
     * 飞塔管理平台移动策略
     * @param
     * @return
     */
    ReturnT<String> fortinetMovePolicy(ManagementPlatformCreatePolicyDTO policyDTO);

}
