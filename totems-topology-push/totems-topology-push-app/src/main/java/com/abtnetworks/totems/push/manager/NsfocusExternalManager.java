package com.abtnetworks.totems.push.manager;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.nsfocus.NsfocusAddressDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateAddressDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreatePolicyDTO;
import com.abtnetworks.totems.push.dto.platform.dto.ManagementPlatformCreateServiceDTO;

import java.util.List;

/**
 * 绿盟外部接口api管理类
 * @author lifei
 * @since 2021/3/5
 **/
public interface NsfocusExternalManager {

    /**
     * 创建ipv4源地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV4SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 创建目的地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV4DstAddressData(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 创建源地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV6SrcAddressData(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 创建目的地址对象
     *
     * @param addressDTO
     * @return
     */
    ReturnT<List<String>> createIPV6DstAddressData(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 创建地址组对象
     * @param addressDTO
     * @return
     */
    ReturnT<String> createAddressDataGroup(ManagementPlatformCreateAddressDTO addressDTO);

    /**
     * 创建服务对象
     *
     * @param serviceDTO
     * @return
     */
    ReturnT<List<String>> createServiceData(ManagementPlatformCreateServiceDTO serviceDTO);

    /**
     * 创建安全策略对象
     *
     * @param policyDTO
     * @return
     */
    ReturnT<String> createSecurityPolicyData(ManagementPlatformCreatePolicyDTO policyDTO);

    /**
     * 创建源nat策略对象
     *
     * @param policyDTO
     * @return
     */
    ReturnT<String> createSNatPolicyData(ManagementPlatformCreatePolicyDTO policyDTO);

    /**
     * 创建源nat策略对象
     *
     * @param policyDTO
     * @return
     */
    ReturnT<String> createDNatPolicyData(ManagementPlatformCreatePolicyDTO policyDTO);

}
