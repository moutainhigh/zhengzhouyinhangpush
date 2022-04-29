package com.abtnetworks.totems.push.manager.impl;

import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.credential.dao.mysql.CredentialMapper;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.push.dto.platform.dto.*;
import com.abtnetworks.totems.push.manager.FortiExternalManager;
import com.abtnetworks.totems.push.manager.FortiManagementPlatformManager;
import com.abtnetworks.totems.recommend.entity.PolicyRecommendCredentialEntity;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lifei
 * @desc
 * @date 2021-02-03 13:59
 */
@Service
public class FortiManagementPlatformManagerImpl implements FortiManagementPlatformManager {

    private static Logger log = LoggerFactory.getLogger(FortiManagementPlatformManagerImpl.class);

    @Autowired
    RecommendTaskManager recommendTaskManager;

    @Autowired
    CredentialMapper credentialMapper;

    @Autowired
    FortiExternalManager fortiExternalManager;

    @Override
    public ReturnT<String> fortinetLogin(ManagementPlatformLoginDTO loginDTO) {
        log.info("根据设备uuid:[{}]获取session开始", loginDTO.getDeviceUuid());
        NodeEntity nodeEntity = getNodeEntity(loginDTO.getDeviceUuid());
        if (nodeEntity == null) {
            log.error("根据设备uuid:[{}]查询节点对象为空!", loginDTO.getDeviceUuid());
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取节点对象失败", loginDTO.getDeviceUuid()));
        }
        // 2.根据凭证id查询凭证的通行证用户名和用户密码
        CredentialEntity credentialEntity = credentialMapper.getByUuid(nodeEntity.getCredentialUuid());
        if (null == credentialEntity) {
            log.error("根据凭证uuid:[{}]查询凭证对象为空!", nodeEntity.getCredentialUuid());
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据凭证uuid:%s 获取凭证对象失败", nodeEntity.getCredentialUuid()));
        }
        // 密码解密
        loginDTO.setUserName(credentialEntity.getEnableUserName());
        loginDTO.setPassword(Encodes.decodeBase64Key(credentialEntity.getEnablePassword()));
        return fortiExternalManager.getToken(loginDTO, nodeEntity.getWebUrl());
    }

    @Override
    public ReturnT<List<String>> fortinetCreateIPV4SrcAddress(ManagementPlatformCreateAddressDTO addressDTO) {
        String session = getSession(addressDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", addressDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createIPV4SrcAddressData(addressDTO, addressDTO.getWebUrl(), session);
    }

    @Override
    public ReturnT<List<String>> fortinetCreateIPV4DstAddress(ManagementPlatformCreateAddressDTO addressDTO) {
        String session = getSession(addressDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", addressDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createIPV4DstAddressData(addressDTO, addressDTO.getWebUrl(), getSession(addressDTO.getDeviceUuid()));
    }

    @Override
    public ReturnT<List<String>> fortinetCreateIPV6SrcAddress(ManagementPlatformCreateAddressDTO addressDTO) {
        String session = getSession(addressDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", addressDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createIPV6SrcAddressData(addressDTO, addressDTO.getWebUrl(), getSession(addressDTO.getDeviceUuid()));
    }

    @Override
    public ReturnT<List<String>> fortinetCreateIPV6DstAddress(ManagementPlatformCreateAddressDTO addressDTO) {
        String session = getSession(addressDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", addressDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createIPV6DstAddressData(addressDTO, addressDTO.getWebUrl(), getSession(addressDTO.getDeviceUuid()));
    }

    @Override
    public ReturnT<String> getPackageNameByDeviceName(ManagementPlatformGetPackageDTO packageDTO) {
        return fortiExternalManager.getPackageName(packageDTO.getDeviceName(),packageDTO.getVsysName(),packageDTO.getWebUrl(), getSession(packageDTO.getDeviceUuid()));
    }

    @Override
    public ReturnT<List<String>> fortinetCreateServiceObject(ManagementPlatformCreateServiceDTO serviceDTO) {
        String session = getSession(serviceDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", serviceDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createServiceData(serviceDTO, serviceDTO.getWebUrl(), session);
    }

    @Override
    public ReturnT<String> fortinetCreateTimeObject(ManagementPlatformCreateTimeDTO timeDTO) {
        String session = getSession(timeDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", timeDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createTimeData(timeDTO, timeDTO.getWebUrl(), getSession(timeDTO.getDeviceUuid()));
    }

    @Override
    public ReturnT<String> fortinetCreateIPV4Policy(ManagementPlatformCreatePolicyDTO policyDTO) {
        String session = getSession(policyDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", policyDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createPolicyData(policyDTO, policyDTO.getWebUrl(), getSession(policyDTO.getDeviceUuid()), false);
    }

    @Override
    public ReturnT<String> fortinetCreateIPV6Policy(ManagementPlatformCreatePolicyDTO policyDTO) {
        String session = getSession(policyDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", policyDTO.getDeviceUuid()));
        }
        return fortiExternalManager.createPolicyData(policyDTO, policyDTO.getWebUrl(), session, true);
    }

    @Override
    public ReturnT<String> fortinetInstall(ManagementPlatformInstallDTO installDTO) {
        String session = getSession(installDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", installDTO.getDeviceUuid()));
        }
        return fortiExternalManager.fortinetInstall(installDTO, installDTO.getWebUrl(), session);
    }

    @Override
    public ReturnT<String> fortinetDeletePolicy(ManagementPlatformCreatePolicyDTO policyDTO) {
        String session = getSession(policyDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", policyDTO.getDeviceUuid()));
        }
        return fortiExternalManager.deletePolicyData(policyDTO, policyDTO.getWebUrl(), session);
    }

    @Override
    public ReturnT<String> fortinetMovePolicy(ManagementPlatformCreatePolicyDTO policyDTO) {
        String session = getSession(policyDTO.getDeviceUuid());
        if (AliStringUtils.isEmpty(session)) {
            return new ReturnT(ReturnT.FAIL_CODE, String.format("根据设备uuid:%s 获取seesion失败", policyDTO.getDeviceUuid()));
        }
        return fortiExternalManager.movePolicyData(policyDTO, policyDTO.getWebUrl(), session);
    }

    /**
     * @param deviceUuid
     * @return
     */
    private NodeEntity getNodeEntity(String deviceUuid) {
        // 1.根据设备查询设备信息
        NodeEntity nodeEntity = recommendTaskManager.getTheNodeByUuid(deviceUuid);
        if (null == nodeEntity) {
            log.error("根据设备uuid:[{}]查询节点对象为空!", deviceUuid);
            return null;
        }
        if (AliStringUtils.isEmpty(nodeEntity.getWebUrl())) {
            log.error("根据设备uuid:[{}]查询节点对象对应的深信服与checkPoint地址标记为空!", deviceUuid);
            return null;
        }
        return nodeEntity;
    }

    /**
     * @param deviceUuid
     * @return
     */
    private String getSession(String deviceUuid) {
        // 查询设备信息，获取webUrl
        log.info("根据设备uuid:[{}]查询认证开始", deviceUuid);
        NodeEntity nodeEntity = getNodeEntity(deviceUuid);
        log.info("查询到设备信息为：{}", JSON.toJSONString(nodeEntity));
        if (nodeEntity == null) {
            return null;
        }
        // 2.根据凭证id查询凭证的通行证用户名和用户密码
        CredentialEntity credentialEntity = credentialMapper.getByUuid(nodeEntity.getCredentialUuid());
        log.info("查询到凭据信息为：{}", JSON.toJSONString(credentialEntity));
        if (null == credentialEntity) {
            log.error("根据凭证uuid:[{}]查询节点对象为空!", nodeEntity.getCredentialUuid());
            return null;
        }
        ManagementPlatformLoginDTO loginDTO = new ManagementPlatformLoginDTO();
        // 密码解密
        loginDTO.setUserName(credentialEntity.getEnableUserName());
        loginDTO.setPassword(Encodes.decodeBase64Key(credentialEntity.getEnablePassword()));
        ReturnT<String> returnT = fortiExternalManager.getToken(loginDTO, nodeEntity.getWebUrl());
        if (null != returnT && ReturnT.SUCCESS_CODE == returnT.getCode()) {
            return returnT.getData();
        } else {
            return null;
        }
    }

}
