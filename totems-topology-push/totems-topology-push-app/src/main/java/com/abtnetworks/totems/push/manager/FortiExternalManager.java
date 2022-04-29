package com.abtnetworks.totems.push.manager;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.platform.dto.*;

import java.util.List;

/**
 * push外部接口api管理类
 *
 * @author lifei
 * @since 2021/2/3
 **/
public interface FortiExternalManager {

    /**
     * 根据用户名和密码和请求ip获取token
     *
     * @param requestIp
     * @return
     */
    ReturnT<String> getToken(ManagementPlatformLoginDTO loginDTO, String requestIp);

    /**
     * 根据设备名称匹配包名称
     *
     * @param deviceName
     * @param requestIp
     * @param session
     * @return
     */
    ReturnT<String> getPackageName(String deviceName,String vsysName, String requestIp, String session);

    /**
     * 创建ipv4源地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV4SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session);

    /**
     * 创建目的地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV4DstAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session);

    /**
     * 创建源地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV6SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session);

    /**
     * 创建目的地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV6DstAddressData(ManagementPlatformCreateAddressDTO addressDTO, String requestIp, String session);

    /**
     * 创建服务对象
     *
     * @param serviceDTO
     * @return
     */
    ReturnT<List<String>> createServiceData(ManagementPlatformCreateServiceDTO serviceDTO, String requestIp, String session);

    /**
     * 创建时间对象
     *
     * @param timeDTO
     * @return
     */
    ReturnT<String> createTimeData(ManagementPlatformCreateTimeDTO timeDTO, String requestIp, String session);

    /**
     * 创建策略
     *
     * @param policyDTO
     * @return
     */
    ReturnT<String> createPolicyData(ManagementPlatformCreatePolicyDTO policyDTO, String requestIp, String session, boolean ipv6);

    /**
     * 删除策略
     *
     * @param policyDTO
     * @return
     */
    ReturnT<String> deletePolicyData(ManagementPlatformCreatePolicyDTO policyDTO, String requestIp, String session);

    /**
     * 安装
     *
     * @param installDTO
     * @return
     */
    ReturnT<String> fortinetInstall(ManagementPlatformInstallDTO installDTO, String requestIp, String session);

    /**
     * 移动策略
     *
     * @param policyDTO
     * @return
     */
    ReturnT<String> movePolicyData(ManagementPlatformCreatePolicyDTO policyDTO, String requestIp, String session);

}
