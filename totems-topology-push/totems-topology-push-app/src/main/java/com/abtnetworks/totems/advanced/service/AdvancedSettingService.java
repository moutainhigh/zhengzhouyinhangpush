package com.abtnetworks.totems.advanced.service;

import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.vo.DeviceInfoAndBusinessVO;
import com.abtnetworks.totems.advanced.vo.DeviceInfoVO;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface AdvancedSettingService {

    /**
     * 获取设备UUID列表
     * @param paramName 参数名称
     * @return 设备UUID列表
     */
    List<String> getDeviceUuidList(String paramName);

    /**
     * 获取设备列表
     * @param paramName 参数名称
     * @return 设备列表
     */
    List<DeviceInfoVO> getDeviceList(String paramName);

    /**
     * 获取设备列表
     * @param paramName 参数名称
     * @return 设备列表
     */
    List<DeviceInfoVO> getDeviceSingList(String paramName);

    /**
     * 获取不在范围内设备列表
     * @param paramName
     * @return 设备列表
     */
    List<DeviceInfoVO> getUnselectedDeviceList(String paramName);

    /**
     * 获取没有选择的设备列表
     * @return 设备列表
     */
    List<DeviceInfoVO> getUnselectedZoneDeviceList();

    /**
     * 从设备列表中删除设备
     * @param paramName 参数名称
     * @param deviceUuid 设备UUID
     * @return 删除结果
     */
    int removeDeviceFromList(String paramName, String deviceUuid);

    /**
     * 从设备列表中删除设备
     * @param paramName
     * @param deviceUuid
     * @return
     */
    int  removeDeviceSingFromList(String paramName, String deviceUuid,String connecType);

    /**
     * 添加设备到设备列表
     * @param paramName 参数名称
     * @param list 设备列表
     * @return 添加结果
     */
    int addDeviceToList(String paramName, List<String> list);

    /**
     * 添加设备到设备列表
     * @param paramName 参数名称
     * @param list 设备列表
     * @return 添加结果
     */
    int addDeviceToListForNotMove(String paramName, List<String> list,String connectType);

    /**
     * 获取参数值
     * @param paramName 参数名称
     * @return 参数值
     */
    String getParamValue(String paramName);

    /**
     * 设置参数
     * @param paramName 参数名称
     * @param paramValue 参数值
     * @return 返回设置结果
     */
    int setParamValue(String paramName, String paramValue);

    /**
     * 检测设备是否在里外列表当中
     * @param paramName 参数名称
     * @param deviceUuid 设备UUID
     * @return 在则返回true，否则fanhuifalse
     */
    boolean isDeviceInTheList(String paramName, String deviceUuid);

    /**
     * 获取移动到某条策略之前/之后的设备列表
     * @param paramName 参数名称
     * @return 设备列表
     */
    List<DeviceInfoVO> getMovePolicyDeviceList(String paramName);

    /**
     * 移除移动到某条策略之前/之后的设备
     * @param paramName 参数名称
     * @param deviceUuid 设备UUID
     * @return 结果
     */
    int removeMovePolicyDevice(String paramName, String deviceUuid);

    /**
     * 移除移动到某条策略之前/之后的设备
     * @param paramName 参数名称
     * @param deviceUuid 设备UUID
     * @return 结果
     */
    int removeMovePolicyDeviceForbeforeAndAfter(String paramName, String deviceUuid,String connectType);

    /**
     * 添加设备到某条策略之前/之后的设备列表
     * @param paramName 参数名称
     * @param uuidList 设备UUID列表
     * @return 结果
     */
    int addMovePolicyDevice(String paramName, List<String> uuidList,String connectType);

    /**
     * 添加设备到某条策略之前/之后的设备列表
     * @param paramName 参数名称
     * @param uuidList 设备UUID列表
     * @return 结果
     */
    int addMovePolicyDeviceForBeforeOrAfter(String paramName, List<String> uuidList,String connectType);

    /**
     * 设置添加到某条策略之前/之后的策略
     * @param paramName 参数名称
     * @param deviceUuid 设备UUID
     * @param policy 策略名称
     * @return 结果
     */
    int setMovePolicyDevicePolicy(String paramName, String deviceUuid, String policy,String connectType);

    /**
     * 获取未选择设备列表
     * @return 未选择设备列表
     */
    List<DeviceInfoVO> getUnselectedMovePolicyDeviceList(String connectType);

    /**
     * 获取添加到某条策略之前/之后的设备信息
     * @param paramName 参数名称
     * @param deviceUuid 设备名称
     * @return 设备数据
     */
    DeviceDTO getMovePolicyDevice(String paramName, String deviceUuid);

    /**
     * 获取添加到某条策略之前/之后的设备信息
     * @param paramName 参数名称
     * @param deviceUuid 设备名称
     * @param connectType 连接类型
     * @return 设备数据
     */
    DeviceDTO getMovePolicyDeviceByType(String paramName, String deviceUuid,String connectType);

    int setFortinetPolicyId(String deviceUuid, Integer id);

    int setCiscoASAPolicyId(String deviceUuid, Integer id);

    int setFortinetStaticRouteId(String deviceUuid, Integer id);

    int getFortinetPolicyId(String deviceUuid);

    int getCiscoASAPolicyId(String deviceUuid);

    int getFortinetStaticRouteId(String deviceUuid);

    int setAbtnetworksPolicyId(String deviceUuid, Integer id,String paramType);

    int getAbtnetworksOrSdnwarePolicyId(String deviceUuid,String paramType);

    int setAbtnetworksPolicy6Id(String deviceUuid, Integer id,String paramType);

    int getAbtnetworksOrSdnwarePolicy6Id(String deviceUuid,String paramType);

    int setAnhengPolicyId(String deviceUuid, Integer id);

    int getAnhengPolicyId(String deviceUuid);

    int setAnhengPolicy6Id(String deviceUuid, Integer id);

    int getAnhengPolicy6Id(String deviceUuid);

    int setMaipuPolicyId(String deviceUuid, Integer id);

    int getMaipuPolicyId(String deviceUuid);

    int setMaipuPolicy6Id(String deviceUuid, Integer id);

    int getMaipuPolicy6Id(String deviceUuid);

    int setWestonePolicyId(String deviceUuid, Integer id);

    int getWestonePolicyId(String deviceUuid);

    int setWestonePolicy6Id(String deviceUuid, Integer id);

    int getWestonePolicy6Id(String deviceUuid);

    int setWestoneNatPolicyId(String deviceUuid, Integer id,PolicyEnum policyType);

    int getWestoneNatPolicyId(String deviceUuid, PolicyEnum policyType);

    /**
     * 设置h3c最大地址池id
     * @param deviceUuid
     * @param id
     * @return
     */
    int setH3cAddressGroupId(String deviceUuid, Integer id);

    /**
     * 获取
     * @param deviceUuid
     * @return
     */
    int getH3cAddressGroupId(String deviceUuid);


    int setCiscoRoutePolicyId(String deviceUuid, List<Integer> id,Integer taskId);
    /**
     * 根据策略集uuid获取该策略集下面已经使用过的ruleId
     * @param deviceUuid
     * @return
     */
    List<Integer> getCiscoRoutePolicyId(String deviceUuid);

    /**
     * 根据工单号删除思科已经使用过的ruleId
     * @param taskId
     */
    void removeRuleIdByTaskId(Integer taskId);

    /***
     * 获取该设备的当前移动策略
     *
     * @param deviceUuid
     * @return
     */
    MoveParamDTO getMoveByDeviceUuidAndParam( String deviceUuid,String connectType);

    /**
     * 添加天融信分组名称
     * @param deviceUuid
     * @param groupName
     * @return
     */
    int addTopSecGroupName(String deviceUuid, String groupName);

    /**
     * 获取未选择设备 依据value的json对象
     * @param paramName 参数名称
     * @return  排除已选择的设备外，其他所有防火墙设备
     */
    List<DeviceInfoVO> getUnselectedDeviceByJson(String paramName);

    /**
     * 添加CheckPoint的layer，package
     * @param deviceUuid
     * @param layerName
     * @param  policyPackage
     * @return
     */
    int addCheckPointLayerPackage(String deviceUuid, String layerName,String policyPackage);

    /**
     * 获取未选择且相同厂商设备 依据value的json对象
     * @param paramName 参数名称
     * @return  排除已选择的设备外，其他所有防火墙设备
     */
    List<DeviceInfoVO> getUnselectedSameVendorNameDeviceByJson(String paramName);

    /**
     * 添加相同厂商设备到设备列表
     * @param paramName 参数名称
     * @param list 设备列表
     * @return 添加结果
     */
    int addSameVendorNameDeviceToList(String paramName, List<String> list);

    /**
     * 获取已选择的主备双活列表
     * @param paramName
     * @return
     */
    List<List<DeviceInfoVO>> selectedActiveStandbyList(String paramName);

    /**
     * 移除主备双活设备
     * @param paramName
     * @param deviceUuids
     * @return
     */
    int removeActiveStandbyDevice(String paramName, String deviceUuids);

    /**
     * 移除灾备设备
     * @param paramName
     * @param deviceUuids
     * @return
     */
    int removeDisasterRecoveryDevice(String paramName, String deviceUuids);

    /**
     * 根据设备ip查询主备/灾备对应的另一个设备
     * @param paramName
     * @param deviceIp
     * @return
     */
    List<NodeEntity> getAnotherDeviceByIp(String paramName, String deviceIp);

    /**
     * 添加相同厂商设备到设备列表
     * @param paramName 参数名称
     * @param list 设备列表
     * @return 添加结果
     */
    int addDisasterRecovery(String paramName, List<String> list);

    /**
     * 上传下发特例文件
     * @param paramName
     * @param file
     * @param deviceUuid
     * @return
     */
    int uploadPyFile(String paramName,MultipartFile file, String deviceUuid);

    /**
     * 新增下发特例设备数据
     * @param paramName
     * @param deviceUuid
     * @return
     */
    int addPushSpecialDevice(String paramName,String deviceUuid);


    /**
     * 上传下发特例文件
     * @param paramName
     * @param deviceUuid
     * @return
     */
    int deletePyFile(String paramName,String deviceUuid);

    /**
     * 查询所有下发特例的文件
     * @param paramName
     * @return
     */
    List<DeviceInfoAndBusinessVO> queryAllPushFile(String paramName);

    /**
     * 查询所有下发特例的文件的设备列表
     * @return
     */
    List<DeviceInfoVO> getPushFileDeviceList(String paramName);

    /**
     *
     * @param paramName
     * @param currentIp
     * @return
     */
    String getPythonFileName(String paramName,String currentIp);
}
