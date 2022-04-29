package com.abtnetworks.totems.recommend.dao.mysql;

import com.abtnetworks.totems.common.entity.NodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2018/12/18 11:05
 */
@Mapper
@Repository
public interface NodeMapper {

    /**
     * 根据设备UUID获取采集id
     * @param uuid
     * @return
     */
    String getGatherIdByDeviceUuid(String uuid);

    /**
     * 根据设备UUID获取采集状态
     * @param uuid
     * @return
     */
    int getGatherStateByDeviceUuid(String uuid);

    /***
     * 根据设备UUID获取认证信息UUID
     * @param uuid 设备UUID
     * @return 认证信息UUID
     */
    String getCredentialUuidByDeviceUuid(String uuid);

    /**
     * 根据设备uuid获取管理ip
     * @param uuid 设备uuid
     * @return 管理ip
     */
    String getDeviceManageIp(String uuid);

    /**
     * 根据设备ip模糊查询所有的设备
     * @param ip 管理ip
     * @return 设备
     */
    List<NodeEntity> listVsysDetailByIp(@Param("ip") String ip);

    /***
     * 根据设备uuid获取设备vendor id
     * @param uuid 设备uuid
     * @return 设备VendorId
     */
    String getDeviceVendorId(String uuid);

    /**
     * 根据设备uuid获取设备id
     * @param uuid 设备uuid
     * @return 设备id
     */
    String getDeviceId(String uuid);

    /**
     * 根据设备uuid获取设备名称
     * @param uuid 设备uuid
     * @return 设备名称
     */
    String getDeviceName(String uuid);

    /***
     * 根据设备uuid获取设备型号
     * @param uuid 设备uuid
     * @return 设备型号
     */
    String getDeviceModelNumber(String uuid);

    /**
     * 根据采集凭据UUID获取设备UUID集合
     * @param credentialUuid
     * @return
     */
    List<String> getNodeUuidsByCredentialUuid(String credentialUuid);

    NodeEntity getTheNodeByUuid(@Param("deviceUuid") String deviceUuid);

    /**
     * 根据设备UUID获取采集端口
     * @param uuid
     * @return
     */
    Integer getDeviceGatherPort(String uuid);

    List<NodeEntity> getNodeList();

    /**
     * 根据设备IP地址获取设备数据
     * @param deviceIp 设备IP
     * @return 设备数据
     */
    NodeEntity getTheNodeByIp(@Param("deviceIp") String deviceIp);

    /**
     * 根据设备IP地址获取设备数据
     * @param ipList 设备IP
     * @return 设备数据
     */
    List<NodeEntity> getTheNodeByIpList(@Param("ipList") List<String> ipList);

    /**
     * 根据uuid查询到引用的设备
     * @param credentialUuid
     * @return
     */
    List<NodeEntity> getNodeByCredentialUuid(String credentialUuid);
}
