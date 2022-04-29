package com.abtnetworks.totems.advanced.service.impl;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.advanced.dao.mysql.AdvanceSettingsMapper;
import com.abtnetworks.totems.advanced.dto.DeviceDTO;
import com.abtnetworks.totems.advanced.dto.DeviceListDTO;
import com.abtnetworks.totems.advanced.dto.DeviceSingDTO;
import com.abtnetworks.totems.advanced.entity.AdvanceSettingsEntity;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.advanced.vo.DeviceInfoAndBusinessVO;
import com.abtnetworks.totems.advanced.vo.DeviceInfoVO;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.ConnectTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.io.TotemsFileUtils;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
public class AdvancedSettingServiceImpl implements AdvancedSettingService {

    private static Logger logger = LoggerFactory.getLogger(AdvancedSettingServiceImpl.class);

    private static final String DEVICE_LIST_SEPERATOR = ",";

    @Value("${python-directory.fileDir}")
    private String pyFileBasedir;

    @Autowired
    private AdvanceSettingsMapper advanceSettingsMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private LogClientSimple logClientSimple;

    @Autowired
    public WhaleManager whaleService;

    @Override
    public List<String> getDeviceUuidList(String paramName) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        List<String> deviceUuidList = new ArrayList<>();

        if(list.size() == 0) {
            return deviceUuidList;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        DeviceListDTO deviceListDTO = JSONObject.parseObject(value,DeviceListDTO.class);

        if(deviceListDTO.getDevices() == null) {
            return deviceUuidList;
        }

        return deviceListDTO.getDevices();
    }

    @Override
    public List<DeviceInfoVO> getDeviceList(String paramName) {
        List<String> deviceUuidList = getDeviceUuidList(paramName);
        List<DeviceInfoVO> deviceList = new ArrayList<>();
        for(String deviceUuid:deviceUuidList) {
            if(AliStringUtils.isEmpty(deviceUuid)) {
                continue;
            }
            logger.info(String.format("获取设备(%s)节点信息", deviceUuid));
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.info("节点信息为空！");
                continue;
            }
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setUuid(deviceUuid);
            deviceList.add(deviceInfoVO);
        }

        return deviceList;
    }

    @Override
    public List<DeviceInfoVO> getDeviceSingList(String paramName) {
        List<DeviceSingDTO> deviceUuidList = getDeviceLists(paramName);
        List<DeviceInfoVO> deviceList = new ArrayList<>();
        for(DeviceSingDTO deviceSingDTO:deviceUuidList) {
            String  deviceUuid = deviceSingDTO.getDeviceUuid();
            logger.info(String.format("获取设备(%s)节点信息", deviceUuid));
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.info("节点信息为空！");
                continue;
            }
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setUuid(deviceUuid);
            deviceInfoVO.setConnectType(StringUtils.isBlank(deviceSingDTO.getConnectType()) ? ConnectTypeEnum.SHORT_CONNECT.getCode().toString() :
                    deviceSingDTO.getConnectType());
            deviceList.add(deviceInfoVO);
        }

        return deviceList;
    }

    @Override
    public List<DeviceInfoVO> getUnselectedDeviceList(String paramName) {
        List<String> deviceUuidList = getDeviceUuidList(paramName);
        List<NodeEntity> nodeEntityList = getNodeList();
        List<DeviceInfoVO> deviceList = new ArrayList<>();
        for(NodeEntity nodeEntity:nodeEntityList) {
            if(!nodeEntity.getType().equals("0") && !nodeEntity.getType().equals("1")) {
                logger.info(String.format("设备[%s](%s)为非防火墙/路由交换类型，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                continue;
            }
            if(deviceUuidList.contains(nodeEntity.getUuid())) {
                logger.info(String.format("设备(%s)已在列表中...", nodeEntity.getUuid()));
                continue;
            }
            //获取设备信息
            DeviceRO device = whaleService.getDeviceByUuid(nodeEntity.getUuid());
            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.error(String.format("根据设备uuid:%s,查询设备详情为空", nodeEntity.getUuid()));
                continue;
            }
            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            if (deviceData.getIsVsys() != null) {
                deviceInfoVO.setVsysName(deviceData.getVsysName());
                deviceInfoVO.setIsVsys(deviceData.getIsVsys());
            }

            deviceInfoVO.setUuid(nodeEntity.getUuid());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceList.add(deviceInfoVO);
        }
        return deviceList;
    }

    @Override
    public List<DeviceInfoVO> getUnselectedZoneDeviceList() {
        List<String> srcZoneDeviceUuidList = getDeviceUuidList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE);
        List<String> dstZoneDeviceUuidList = getDeviceUuidList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE);
        List<String> noZoneDeviceUuidList = getDeviceUuidList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE);
        List<NodeEntity> nodeEntityList = getNodeList();
        List<DeviceInfoVO> deviceList = new ArrayList<>();

        for(NodeEntity nodeEntity:nodeEntityList) {
            if(!nodeEntity.getType().equals("0")) {
                logger.info(String.format("设备[%s](%s)为非防火墙类型，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                continue;
            }
            if(srcZoneDeviceUuidList.contains(nodeEntity.getUuid())) {
                logger.info(String.format("设备(%s)已在列表(%s)中...", nodeEntity.getUuid(), AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE));
                continue;
            }
            if(dstZoneDeviceUuidList.contains(nodeEntity.getUuid())) {
                logger.info(String.format("设备(%s)已在列表(%s)中...", nodeEntity.getUuid(), AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE));
                continue;
            }
            if(noZoneDeviceUuidList.contains(nodeEntity.getUuid())) {
                logger.info(String.format("设备(%s)已在列表(%s)中...", nodeEntity.getUuid(), AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE));
                continue;
            }

            //获取设备信息
            DeviceRO device = whaleService.getDeviceByUuid(nodeEntity.getUuid());
            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.error(String.format("根据设备uuid:%s,查询设备详情为空", nodeEntity.getUuid()));
                continue;
            }
            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            if (deviceData.getIsVsys() != null) {
                deviceInfoVO.setVsysName(deviceData.getVsysName());
                deviceInfoVO.setIsVsys(deviceData.getIsVsys());
            }

            deviceInfoVO.setUuid(nodeEntity.getUuid());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceList.add(deviceInfoVO);
        }
        return deviceList;
    }

    @Override
    public int removeDeviceSingFromList(String paramName, String deviceUuid,String connectType) {
        List<DeviceSingDTO> deviceUuidList = getDeviceLists(paramName);
        List<DeviceSingDTO> removeDtos = new ArrayList<>();
        for (DeviceSingDTO deviceSingDTO : deviceUuidList){
            if(StringUtils.isBlank(deviceSingDTO.getConnectType())){
                deviceSingDTO.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if(deviceSingDTO.getDeviceUuid().equals(deviceUuid) && connectType.equals(deviceSingDTO.getConnectType())){
                removeDtos.add(deviceSingDTO);
            }
        }
        deviceUuidList.removeAll(removeDtos);
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);

        JSONObject object = new JSONObject();

        String valueString = JSONObject.toJSONString(deviceUuidList);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        String message = getBusinessType(paramName)+",移除设备成功";
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int removeDeviceFromList(String paramName, String deviceUuid) {
        List<String> deviceUuidList = getDeviceUuidList(paramName);
        if(deviceUuidList.contains(deviceUuid)) {
            deviceUuidList.remove(deviceUuid);
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        DeviceListDTO deviceListDTO = new DeviceListDTO();
        deviceListDTO.setDevices(deviceUuidList);
        entity.setParamValue(JSONObject.toJSONString(deviceListDTO));
        advanceSettingsMapper.updateByPrimaryKey(entity);

        String message = getBusinessType(paramName)+",移除设备成功";
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    private String getBusinessType(String paramName){
        String message = "修改高级设置-";
        if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE)){
            message += "ACL挂载接口设置";
        }else if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP)){
            message += "策略移动配置-不移动";
        }else if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE)){
            message += "安全域配置-仅指定源域";
        }else if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE)){
            message += "安全域配置-仅指定目的域";
        }else if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE)){
            message += "安全域配置-不指定域";
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE)){
            message += "策略移动配置-移动冲突策略前";
        }

        return message;
    }

    @Override
    public int addDeviceToList(String paramName, List<String> list) {
        List<String> deviceUuidList = getDeviceUuidList(paramName);

        List<String> removeList = new ArrayList<>();
        for(String deviceUuid : deviceUuidList) {
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                removeList.add(deviceUuid);
            }
        }

        deviceUuidList.removeAll(removeList);

        for(String deviceUuid : list) {
            if(deviceUuidList.contains(deviceUuid)) {
                logger.info(String.format("设备(%s)已在(%s)例外列表中", deviceUuid, paramName));
                continue;
            }
            deviceUuidList.add(deviceUuid);
        }

        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.ADD_DEVICE_TO_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        DeviceListDTO deviceListDTO = new DeviceListDTO();
        deviceListDTO.setDevices(deviceUuidList);
        entity.setParamValue(JSONObject.toJSONString(deviceListDTO));
        advanceSettingsMapper.updateByPrimaryKey(entity);
        String message = getBusinessType(paramName)+",添加设备成功";
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int addDeviceToListForNotMove(String paramName, List<String> list, String connectType) {
        List<DeviceSingDTO> deviceUuidList = getDeviceLists(paramName);

        List<DeviceSingDTO> removeList = new ArrayList<>();
        for(DeviceSingDTO device : deviceUuidList) {
            String deviceUuid = device.getDeviceUuid();
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                removeList.add(device);
            }
        }

        deviceUuidList.removeAll(removeList);

        // 按照设备维度统计连接类型
        Map<String, List<String>> deviceMap = buildDeviceconnectTypeMap();

        for(String uuid : list) {
            if (deviceMap.containsKey(uuid)) {
                List<String> types = deviceMap.get(uuid);
                boolean containAllTypes = isContainConnectType(types,connectType);
                if (containAllTypes) {
                    logger.info(String.format("设备(%s)的连接方式已经在配置列表中...", uuid));
                    continue;
                }
            }
            DeviceSingDTO deviceDTO = new DeviceSingDTO();
            deviceDTO.setDeviceUuid(uuid);
            deviceDTO.setConnectType(connectType);
            deviceUuidList.add(deviceDTO);
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，添加失败...");
            return ReturnCode.ADD_DEVICE_TO_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(deviceUuidList);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        String paramNameDesc = "不移动";
        if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE)){
            paramNameDesc = "移动冲突策略前";
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-策略移动配置-" + paramNameDesc + "，添加设备成功");

        return ReturnCode.POLICY_MSG_OK;

    }

    private List<DeviceSingDTO> getDeviceLists(String paramName) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        List<DeviceSingDTO> deviceDTOList = new ArrayList<>();
        if(list.size() == 0) {
            return deviceDTOList;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONObject jsonObject = JSONObject.parseObject(value);
        JSONArray jsonArray = jsonObject.getJSONArray("devices");
        for(int index =0; index < jsonArray.size(); index ++) {
            JSONObject deviceObject = jsonArray.getJSONObject(index);
            DeviceSingDTO device = JSONObject.toJavaObject(deviceObject, DeviceSingDTO.class);
            deviceDTOList.add(device);
        }

        return deviceDTOList;
    }

    @Override
    public String getParamValue(String paramName) {
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null ||entityList.size() == 0) {
            return null;
        }
        return entityList.get(0).getParamValue();
    }

    @Override
    public int setParamValue(String paramName, String paramValue) {
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE) || paramName.equals(AdvancedSettingsConstants.PARAM_NAME_GLOBLE_OR_SECURITY) || paramName.equalsIgnoreCase(AdvancedSettingsConstants.PARAM_NAME_CONFIG_PUSH_TIME_LOCK)) {
            if (entityList == null || entityList.size() == 0) {
                AdvanceSettingsEntity entity = new AdvanceSettingsEntity(paramName,paramValue);
                advanceSettingsMapper.insert(entity);
            }else {
                AdvanceSettingsEntity entity = entityList.get(0);
                entity.setParamValue(paramValue);
                advanceSettingsMapper.updateByPrimaryKey(entity);
            }
        }else {
            if (entityList == null || entityList.size() == 0) {
                logger.info("参数信息不存在，修改失败...");
                return ReturnCode.MODIFY_PARAM_VALUE_FAILED;
            }

            AdvanceSettingsEntity entity = entityList.get(0);
            entity.setParamValue(paramValue);
            advanceSettingsMapper.updateByPrimaryKey(entity);
        }

        String message = "";
        if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CHECK_RULE)) {
            message = String.format("修改高级设置-策略检查配置-模拟仿真时%s", paramValue.equals("1") ? "进行策略检查" : "不检查策略");
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT)) {
            message = String.format("修改高级设置-创建对象配置-生成命令行时，地址或服务%s", paramValue.equals("1") ? "要创建对象" : "不建对象");
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT)) {
            message = String.format("修改高级设置-地址对象复用配置-生成命令行时，%s", paramValue.equals("1") ? "使用已有对象" : "不使用已有对象");
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_OBJECT)) {
            message = String.format("修改高级设置-服务对象复用配置-生成命令行时，%s", paramValue.equals("1") ? "使用已有对象" : "不使用已有对象");
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE)) {
            message = String.format("修改高级设置-山石防火墙回滚命令行时根据策略，%s", paramValue.equals("1") ? "ID" : "名称");
        }else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_GLOBLE_OR_SECURITY)) {
            message = String.format("修改高级设置-juniper防火墙是根据%s生成命令行", paramValue.equals("1") ? "全局地址" : "安全域地址");
        } else if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_CONFIG_PUSH_TIME_LOCK)){
            message = "修改高级设置-全局下发时间锁";
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL)) {
            message = String.format("修改高级设置-用户是否开启接收邮件，%s", paramValue.equals("1") ? "开启" : "关闭");
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MANAGER_EMAIL)) {
            message = String.format("修改高级设置-管理员邮箱，%s", paramValue);
        }else {
            message = "未知";
            logger.error("未知参数,paramName:" + paramName);
        }

        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public boolean isDeviceInTheList(String paramName, String deviceUuid) {
        List<String> deviceUuidList = getDeviceUuidList(paramName);
        if(deviceUuidList.contains(deviceUuid)) {
            return true;
        }

        return false;
    }

    @Override
    public List<DeviceInfoVO> getMovePolicyDeviceList(String paramName){
        List<DeviceDTO> deviceDTOList = getDeviceDTOList(paramName);
        List<DeviceInfoVO> deviceList = new ArrayList<>();

        for(DeviceDTO device:deviceDTOList) {
            String deviceUuid = device.getDeviceUuid();
            logger.info(String.format("获取设备(%s)节点信息", deviceUuid));
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if (nodeEntity == null) {
                logger.info("节点信息为空！");
                continue;
            }
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setUuid(deviceUuid);
            if(device.getRelatedRule() != null){
                deviceInfoVO.setRelatedRule(device.getRelatedRule());
            }

            if (device.getGroupName() != null) {
                deviceInfoVO.setGroupName(device.getGroupName());
            }
            deviceInfoVO.setLayerName(device.getLayerName());
            deviceInfoVO.setPolicyPackage(device.getPolicyPackage());
            deviceInfoVO.setConnectType(StringUtils.isBlank(device.getConnectType()) ? ConnectTypeEnum.SHORT_CONNECT.getCode().toString() :
                    device.getConnectType());
            deviceList.add(deviceInfoVO);
        }

        return deviceList;
    }

    @Override
    public int removeMovePolicyDevice(String paramName, String deviceUuid) {
        List<DeviceDTO> list = getDeviceDTOList(paramName);

        for (DeviceDTO device : list) {
            if (device.getDeviceUuid().equals(deviceUuid)) {
                list.remove(device);
                break;
            }
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if (entityList == null || entityList.size() == 0) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(list);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);
        String paramNameDesc = "策略移动配置-移动到指定位置前";
        if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER)) {
            paramNameDesc = "策略移动配置-移动到指定位置后";
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE)) {
            paramNameDesc = "策略移动配置-移动到指定位置前";
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME)) {
            paramNameDesc = "天融信分组设置";
        }else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY)) {
            paramNameDesc = "主备双活设置";
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-" + paramNameDesc + "，删除设备成功");
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int removeMovePolicyDeviceForbeforeAndAfter(String paramName, String deviceUuid, String connectType) {
        List<DeviceDTO> list = getDeviceDTOList(paramName);

        for (DeviceDTO device : list) {
            if(StringUtils.isBlank(device.getConnectType())){
                device.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if (device.getDeviceUuid().equals(deviceUuid) && connectType.equals(device.getConnectType())) {
                list.remove(device);
                break;
            }
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if (entityList == null || entityList.size() == 0) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(list);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);
        String paramNameDesc = "策略移动配置-移动到指定位置前";
        if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER)) {
            paramNameDesc = "策略移动配置-移动到指定位置后";
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE)) {
            paramNameDesc = "策略移动配置-移动到指定位置前";
        } else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME)) {
            paramNameDesc = "天融信分组设置";
        }else if (paramName.equals(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY)) {
            paramNameDesc = "主备双活设置";
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-" + paramNameDesc + "，删除设备成功");
        return ReturnCode.POLICY_MSG_OK;
    }


    @Override
    public int addMovePolicyDevice(String paramName, List<String> uuidList,String connectType) {
        List<DeviceDTO> list =  getDeviceDTOList(paramName);

        List<DeviceDTO> removeList = new ArrayList<>();
        for(DeviceDTO deviceDTO : list) {
            String deviceUuid = deviceDTO.getDeviceUuid();
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                removeList.add(deviceDTO);
            }
        }

        list.removeAll(removeList);

        for(String uuid : uuidList) {
            for (DeviceDTO device : list) {
                if (device.getDeviceUuid().equals(uuid)) {
                    logger.info(String.format("设备(%s)已在列表(%s)中...", uuid, paramName));
                    continue;
                }
            }
            DeviceDTO deviceDTO = new DeviceDTO();
            deviceDTO.setDeviceUuid(uuid);
            deviceDTO.setConnectType(connectType);
            list.add(deviceDTO);
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，添加失败...");
            return ReturnCode.ADD_DEVICE_TO_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(list);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        String paramNameDesc = "移动到指定位置前";
        if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER)){
            paramNameDesc = "移动到指定位置后";
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-策略移动配置-" + paramNameDesc + "，添加设备成功");

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int addMovePolicyDeviceForBeforeOrAfter(String paramName, List<String> uuidList, String connectType) {
        List<DeviceDTO> list =  getDeviceDTOList(paramName);

        List<DeviceDTO> removeList = new ArrayList<>();
        for(DeviceDTO deviceDTO : list) {
            String deviceUuid = deviceDTO.getDeviceUuid();
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                removeList.add(deviceDTO);
            }
        }

        list.removeAll(removeList);

        // 按照设备维度统计连接类型
        Map<String, List<String>> deviceMap = buildDeviceconnectTypeMap();

        for(String uuid : uuidList) {
            if (deviceMap.containsKey(uuid)) {
                List<String> types = deviceMap.get(uuid);
                boolean containAllTypes = isContainConnectType(types,connectType);
                if (containAllTypes) {
                    logger.info(String.format("设备(%s)在长短连接都已经在配置列表中...", uuid));
                    continue;
                }
            }
            DeviceDTO deviceDTO = new DeviceDTO();
            deviceDTO.setDeviceUuid(uuid);
            deviceDTO.setConnectType(connectType);
            list.add(deviceDTO);
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，添加失败...");
            return ReturnCode.ADD_DEVICE_TO_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(list);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        String paramNameDesc = "移动到指定位置前";
        if(paramName.equals(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER)){
            paramNameDesc = "移动到指定位置后";
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-策略移动配置-" + paramNameDesc + "，添加设备成功");

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int setMovePolicyDevicePolicy(String paramName, String deviceUuid, String policy,String connectType) {
        List<DeviceDTO> list =  getDeviceDTOList(paramName);

        for (DeviceDTO device : list) {
            if(StringUtils.isBlank(device.getConnectType())){
                device.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if(device.getDeviceUuid().equalsIgnoreCase(deviceUuid) && connectType.equals(device.getConnectType())) {
                device.setRelatedRule(policy);
                continue;
            }
        }
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(list);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public List<DeviceInfoVO> getUnselectedMovePolicyDeviceList(String connectType) {

        List<NodeEntity> nodeEntityList = getNodeList();
        List<DeviceInfoVO> deviceList = new ArrayList<>();
        // 按照设备维度统计连接类型
        Map<String, List<String>> deviceMap = buildDeviceconnectTypeMap();

        for(NodeEntity nodeEntity:nodeEntityList) {
            if(!nodeEntity.getType().equals("0")) {
                logger.info(String.format("设备[%s](%s)为非防火墙类型，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                continue;
            }

            if (deviceMap.containsKey(nodeEntity.getUuid())) {
                List<String> types = deviceMap.get(nodeEntity.getUuid());
                boolean containAllTypes = isContainConnectType(types,connectType);
                if (containAllTypes) {
                    logger.info(String.format("设备(%s)连接都已经在配置列表中...", nodeEntity.getUuid()));
                    continue;
                }
            }

            //获取设备信息
            DeviceRO device = whaleService.getDeviceByUuid(nodeEntity.getUuid());
            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.error(String.format("根据设备uuid:%s,查询设备详情为空", nodeEntity.getUuid()));
                continue;
            }
            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            if (deviceData.getIsVsys() != null) {
                deviceInfoVO.setVsysName(deviceData.getVsysName());
                deviceInfoVO.setIsVsys(deviceData.getIsVsys());
            }

            deviceInfoVO.setUuid(nodeEntity.getUuid());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceList.add(deviceInfoVO);
        }
        return deviceList;
    }

    /**
     * 构建统计已经配置的连接类型
     * @return
     */
    public Map<String, List<String>> buildDeviceconnectTypeMap() {
        List<DeviceSingDTO> notMoveDeviceList = getDeviceLists(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP);
        List<DeviceDTO> moveBeforeDeviceList = getDeviceDTOList(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE);
        List<DeviceDTO> moveAfterDeviceList = getDeviceDTOList(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER);
        List<DeviceDTO> moveBeforeConflictDeviceList = getDeviceDTOList(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE);

        // 按照设备的维度统计配置
        Map<String,List<String>> deviceMap = new HashMap<>();
        for (DeviceSingDTO deviceSingDTO :notMoveDeviceList){
            if(StringUtils.isBlank(deviceSingDTO.getConnectType())){
                deviceSingDTO.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if(deviceMap.containsKey(deviceSingDTO.getDeviceUuid())){
                List<String> existConnectTypes = deviceMap.get(deviceSingDTO.getDeviceUuid());
                existConnectTypes.add(deviceSingDTO.getConnectType());
            }else{
                String[] connectTypes = deviceSingDTO.getConnectType().split(DEVICE_LIST_SEPERATOR);
                List<String> connectTypeList = new ArrayList<>();
                connectTypeList.addAll(Arrays.asList(connectTypes));
                deviceMap.put(deviceSingDTO.getDeviceUuid(),connectTypeList);
            }
        }

        for (DeviceDTO deviceDTO :moveBeforeDeviceList){
            if(StringUtils.isBlank(deviceDTO.getConnectType())){
                deviceDTO.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if(deviceMap.containsKey(deviceDTO.getDeviceUuid())){
                List<String> existConnectTypes = deviceMap.get(deviceDTO.getDeviceUuid());
                existConnectTypes.add(deviceDTO.getConnectType());
            }else{
                String[] connectTypes = deviceDTO.getConnectType().split(DEVICE_LIST_SEPERATOR);
                List<String> connectTypeList = new ArrayList<>();
                connectTypeList.addAll(Arrays.asList(connectTypes));
                deviceMap.put(deviceDTO.getDeviceUuid(),connectTypeList);
            }
        }

        for (DeviceDTO deviceDTO :moveAfterDeviceList){
            if(StringUtils.isBlank(deviceDTO.getConnectType())){
                deviceDTO.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if(deviceMap.containsKey(deviceDTO.getDeviceUuid())){
                List<String> existConnectTypes = deviceMap.get(deviceDTO.getDeviceUuid());
                existConnectTypes.add(deviceDTO.getConnectType());
            }else{
                String[] connectTypes = deviceDTO.getConnectType().split(DEVICE_LIST_SEPERATOR);
                List<String> connectTypeList = new ArrayList<>();
                connectTypeList.addAll(Arrays.asList(connectTypes));
                deviceMap.put(deviceDTO.getDeviceUuid(),connectTypeList);
            }
        }

        for (DeviceDTO deviceDTO :moveBeforeConflictDeviceList){
            if(StringUtils.isBlank(deviceDTO.getConnectType())){
                deviceDTO.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if(deviceMap.containsKey(deviceDTO.getDeviceUuid())){
                List<String> existConnectTypes = deviceMap.get(deviceDTO.getDeviceUuid());
                existConnectTypes.add(deviceDTO.getConnectType());
            }else{
                String[] connectTypes = deviceDTO.getConnectType().split(DEVICE_LIST_SEPERATOR);
                List<String> connectTypeList = new ArrayList<>();
                connectTypeList.addAll(Arrays.asList(connectTypes));
                deviceMap.put(deviceDTO.getDeviceUuid(),connectTypeList);
            }
        }
        return deviceMap;
    }

    /**
     * 判断当前连接方式是否在配置中
     * @param existConfig
     * @return
     */
    private boolean isContainConnectType(List<String> existConfig,String targetConnectType) {
        if (existConfig.contains(ConnectTypeEnum.SHORT_CONNECT.getCode().toString()) && existConfig.contains(ConnectTypeEnum.LONG_CONNECT.getCode().toString())) {
            return true;
        }
        if(StringUtils.isNotBlank(targetConnectType) && existConfig.contains(targetConnectType)){
            return true;
        }
        return false;
    }

    @Override
    public DeviceDTO getMovePolicyDevice(String paramName, String deviceUuid) {
        List<DeviceDTO> deviceDTOList = getDeviceDTOList(paramName);
        for(DeviceDTO device: deviceDTOList) {
            if(device.getDeviceUuid().equals(deviceUuid)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public DeviceDTO getMovePolicyDeviceByType(String paramName, String deviceUuid, String connectType) {
        List<DeviceDTO> deviceDTOList = getDeviceDTOList(paramName);
        if(StringUtils.isBlank(connectType)){
            connectType = ConnectTypeEnum.SHORT_CONNECT.getCode().toString();
        }
        for(DeviceDTO device: deviceDTOList) {
            if (StringUtils.isBlank(device.getConnectType())) {
                // 设置默认的短连接
                device.setConnectType(ConnectTypeEnum.SHORT_CONNECT.getCode().toString());
            }
            if (device.getDeviceUuid().equals(deviceUuid) && device.getConnectType().equals(connectType)) {
                return device;
            }
        }
        return null;
    }

    @Override
    public int setAbtnetworksPolicyId(String deviceUuid, Integer id,String paramType) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramType);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(paramType);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(paramType);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getAbtnetworksOrSdnwarePolicyId(String deviceUuid,String paramType){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramType);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setAbtnetworksPolicy6Id(String deviceUuid, Integer id,String paramType) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramType);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(paramType);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(paramType);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getAbtnetworksOrSdnwarePolicy6Id(String deviceUuid,String paramType){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramType);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setAnhengPolicyId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getAnhengPolicyId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setAnhengPolicy6Id(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG6_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG6_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG6_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getAnhengPolicy6Id(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_ANHENG6_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setMaipuPolicyId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getMaipuPolicyId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setMaipuPolicy6Id(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU6_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU6_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU6_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getMaipuPolicy6Id(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_MAIPU6_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }
    @Override
    public int setWestonePolicyId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getWestonePolicyId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setWestonePolicy6Id(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE6_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE6_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE6_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getWestonePolicy6Id(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_WESTONE6_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setWestoneNatPolicyId(String deviceUuid, Integer id, PolicyEnum policyType) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        String advancedSettingsConstants = null;
        switch (policyType){
            case SNAT: advancedSettingsConstants = AdvancedSettingsConstants.PARAM_NAME_WESTONE_SRC_NAT_POLICY_ID;break;
            case DNAT: advancedSettingsConstants = AdvancedSettingsConstants.PARAM_NAME_WESTONE_DST_NAT_POLICY_ID;break;
            case STATIC: advancedSettingsConstants = AdvancedSettingsConstants.PARAM_NAME_WESTONE_STATIC_NAT_POLICY_ID;
        }
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(advancedSettingsConstants);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(advancedSettingsConstants);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(advancedSettingsConstants);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int getWestoneNatPolicyId(String deviceUuid, PolicyEnum policyType) {
        String paramName = null;
        switch (policyType){
            case SNAT: paramName = AdvancedSettingsConstants.PARAM_NAME_WESTONE_SRC_NAT_POLICY_ID;break;
            case DNAT: paramName = AdvancedSettingsConstants.PARAM_NAME_WESTONE_DST_NAT_POLICY_ID;break;
            case STATIC: paramName = AdvancedSettingsConstants.PARAM_NAME_WESTONE_STATIC_NAT_POLICY_ID;
        }
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前安博通设备id，返回0");
        return 0;
    }

    @Override
    public int setFortinetPolicyId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int setCiscoASAPolicyId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备策略(%s)最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ASA_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ASA_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ASA_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int setFortinetStaticRouteId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备(%s)静态路由最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_STATIC_ROUTING_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_STATIC_ROUTING_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_STATIC_ROUTING_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    public int getFortinetPolicyId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前飞塔设备id，返回0");
        return 0;
    }

    public int getCiscoASAPolicyId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ASA_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前思科ASA设备id，返回0");
        return 0;
    }

    public int getFortinetStaticRouteId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_FORTINET_STATIC_ROUTING_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前飞塔静态路由id，返回0");
        return 0;
    }

    @Override
    public int setH3cAddressGroupId(String deviceUuid, Integer id) {
        logger.info(String.format("当前设备(%s)地址池最大id为: %d", deviceUuid, id));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_H3V7_ADDRESS_GROUP_ID);
        if(ObjectUtils.isEmpty(list)){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_H3V7_ADDRESS_GROUP_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_H3V7_ADDRESS_GROUP_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasRecord = false;
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                object.put("id", id);
                hasRecord = true;
            }
        }

        if(!hasRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            object.put("id", id);
            jsonArray.add(object);
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int getH3cAddressGroupId(String deviceUuid){
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_H3V7_ADDRESS_GROUP_ID);
        if(ObjectUtils.isEmpty(list)){
            logger.info("数据记录为空...返回0");
            return 0;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                return object.getInteger("id");
            }
        }
        logger.info("找不到当前h3c设备id：{}，返回0", deviceUuid);
        return 0;
    }

    @Override
    public int setCiscoRoutePolicyId(String deviceUuid, List<Integer> ids,Integer taskId) {
        logger.info(String.format("当前策略集(%s)待插入策略集合为: %s", deviceUuid, JSONObject.toJSONString(ids)));
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ROUTE_POLICY_ID);
        if(list == null || list.size() == 0){
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            JSONArray itemJsonArray = new JSONArray();
            JSONObject itemObject = new JSONObject();
            itemObject.put("taskId", taskId);
            itemObject.put("id", ids);
            itemJsonArray.add(itemObject);
            object.put("item", itemJsonArray);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(object);
            AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
            entity.setParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ROUTE_POLICY_ID);
            entity.setParamValue(jsonArray.toJSONString());
            advanceSettingsMapper.insert(entity);
            list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ROUTE_POLICY_ID);
        }

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        boolean hasUuidRecord = false;
        boolean hasItemRecord = false;

        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                JSONArray itemJsonArray = object.getJSONArray("item");
                for (int j = 0; j < itemJsonArray.size(); j++) {
                    JSONObject itemObject = itemJsonArray.getJSONObject(j);
                    Integer itemTaskId = itemObject.getInteger("taskId");
                    if (itemTaskId.equals(taskId)) {
                        JSONArray itemIdJsonArray = itemObject.getJSONArray("id");
                        for (Integer itemId : ids) {
                            if (itemIdJsonArray.contains(itemId)) {
                                continue;
                            } else {
                                itemIdJsonArray.add(itemId);
                            }
                        }
                        hasItemRecord = true;
                    }
                }
                hasUuidRecord = true;
            }
        }

        if(!hasUuidRecord && !hasItemRecord) {
            JSONObject object = new JSONObject();
            object.put("uuid", deviceUuid);
            JSONArray itemJsonArray = new JSONArray();
            JSONObject itemObject = new JSONObject();
            itemObject.put("taskId", taskId);
            itemObject.put("id", ids);
            itemJsonArray.add(itemObject);
            object.put("item", itemJsonArray);
            jsonArray.add(object);
        } else if(hasUuidRecord && !hasItemRecord){
            for (int index = 0; index < jsonArray.size(); index++){
                JSONObject object = jsonArray.getJSONObject(index);
                String uuid = object.getString("uuid");
                if(uuid.equals(deviceUuid)) {
                    JSONArray itemJsonArray = object.getJSONArray("item");
                    JSONObject itemObject = new JSONObject();
                    itemObject.put("taskId", taskId);
                    itemObject.put("id", ids);
                    itemJsonArray.add(itemObject);
                    break;
                }
            }
        }
        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);

        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public List<Integer> getCiscoRoutePolicyId(String deviceUuid) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ROUTE_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空...返回0");
            return new ArrayList<>();
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);
        List<Integer> resultIdArrayList = new ArrayList<>();
        for(int index = 0; index < jsonArray.size(); index++){
            JSONObject object = jsonArray.getJSONObject(index);
            String uuid = object.getString("uuid");
            if(uuid.equals(deviceUuid)) {
                JSONArray itemJsonArray = object.getJSONArray("item");
                for (int j = 0; j < itemJsonArray.size(); j++) {
                    JSONObject itemObject = itemJsonArray.getJSONObject(j);
                    JSONArray idArrayList = itemObject.getJSONArray("id");
                    resultIdArrayList.addAll(JSONObject.parseArray(idArrayList.toJSONString(), Integer.class));
                }
            }
        }
        return resultIdArrayList;
    }


    @Override
    public void removeRuleIdByTaskId(Integer taskId) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(AdvancedSettingsConstants.PARAM_NAME_CISCO_ROUTE_POLICY_ID);
        if(list == null || list.size() == 0){
            logger.info("数据记录为空..不进行删除");
            return;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);

        Iterator<Object> o = jsonArray.iterator();
        while (o.hasNext()) {
            JSONObject object = (JSONObject) o.next();
            JSONArray itemJsonArray = object.getJSONArray("item");
            Iterator<Object> itemObj = itemJsonArray.iterator();
            while (itemObj.hasNext()){
                JSONObject itemObject = (JSONObject) itemObj.next();
                Integer itemTaskId = itemObject.getInteger("taskId");
                if (itemTaskId.equals(taskId)) {
                    itemObj.remove();
                }
                if (0 == itemJsonArray.size()) {
                    o.remove();
                }
            }
        }

        entity.setParamValue(jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);
    }

    private List<DeviceDTO> getDeviceDTOList(String paramName) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        List<DeviceDTO> deviceDTOList = new ArrayList<>();
        if(list.size() == 0) {
            return deviceDTOList;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONObject jsonObject = JSONObject.parseObject(value);
        JSONArray jsonArray = jsonObject.getJSONArray("devices");
        for(int index =0; index < jsonArray.size(); index ++) {
            JSONObject deviceObject = jsonArray.getJSONObject(index);
            DeviceDTO device = JSONObject.toJavaObject(deviceObject, DeviceDTO.class);
            deviceDTOList.add(device);
        }

        return deviceDTOList;
    }

    List<NodeEntity> getNodeList() {
        List<NodeEntity> nodeEntityList = nodeMapper.getNodeList();
        List<NodeEntity> noUuidList = new ArrayList<>();
        for(NodeEntity nodeEntity : nodeEntityList) {
            if(AliStringUtils.isEmpty(nodeEntity.getUuid())){
                noUuidList.add(nodeEntity);
            }
        }
        nodeEntityList.removeAll(noUuidList);
        return nodeEntityList;
    }



    @Override
    public MoveParamDTO getMoveByDeviceUuidAndParam(String deviceUuid,String connectType) {
        DeviceDTO deviceDTO = getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE,deviceUuid,connectType);
        MoveParamDTO moveParamDTO = new MoveParamDTO();
        if(deviceDTO!=null){
            moveParamDTO.setRelatedName(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE);
            moveParamDTO.setRelatedRule(deviceDTO.getRelatedRule());
        }else{
            DeviceDTO deviceDTO1 = getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER,deviceUuid,connectType);
            if(deviceDTO1!=null){
                moveParamDTO.setRelatedName(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER);
                moveParamDTO.setRelatedRule(deviceDTO1.getRelatedRule());
            }else {
                DeviceDTO topDevice = getMovePolicyDeviceByType(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP, deviceUuid,connectType);
                //不移动标记
                if (topDevice != null) {
                    moveParamDTO.setRelatedName(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP);
                } else {
                    //都没有就默认置顶
                    logger.info("{}没有设置移动", deviceUuid);
                }
            }
        }
        return moveParamDTO;
    }


    @Override
    public int addTopSecGroupName(String deviceUuid, String groupName) {
        String paramName = AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME;
        List<DeviceDTO> list = getDeviceDTOList(paramName);

        for (DeviceDTO device : list) {
            if (device.getDeviceUuid().equalsIgnoreCase(deviceUuid)) {
                device.setGroupName(groupName);
                continue;
            }
        }
        int returnCode =  commonUpdateSetting( paramName, list);

        return returnCode;
    }

    /***
     * 公共的修改参数方式
     * @param paramName
     * @param list
     * @return
     */
    private int commonUpdateSetting(String paramName,List<DeviceDTO> list){
        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        if (entityList == null || entityList.size() == 0) {
            logger.info("参数信息不存在，移除失败...");
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(list);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public List<DeviceInfoVO> getUnselectedDeviceByJson(String paramName) {
        List<DeviceDTO> selectedDeviceList = getDeviceDTOList(paramName);
        List<NodeEntity> nodeEntityList = getNodeList();
        List<DeviceInfoVO> deviceList = new ArrayList<>();

        for (NodeEntity nodeEntity : nodeEntityList) {
            if (!nodeEntity.getType().equals("0")) {
                logger.info(String.format("设备[%s](%s)为非防火墙类型，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                continue;
            }

            boolean existDevice = false;
            for (DeviceDTO device : selectedDeviceList) {
                if (device.getDeviceUuid().equals(nodeEntity.getUuid())) {
                    logger.info(String.format("设备(%s)已在列表(%s)中...", nodeEntity.getUuid(), AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE));
                    existDevice = true;
                    break;
                }
            }
            if (existDevice) {
                continue;
            }
            //获取设备信息
            DeviceRO device = whaleService.getDeviceByUuid(nodeEntity.getUuid());
            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.error(String.format("根据设备uuid:%s,查询设备详情为空", nodeEntity.getUuid()));
                continue;
            }
            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            if (deviceData.getIsVsys() != null) {
                deviceInfoVO.setVsysName(deviceData.getVsysName());
                deviceInfoVO.setIsVsys(deviceData.getIsVsys());
            }

            deviceInfoVO.setUuid(nodeEntity.getUuid());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceList.add(deviceInfoVO);
        }
        return deviceList;
    }

    @Override
    public int addCheckPointLayerPackage(String deviceUuid, String layerName, String policyPackage) {

        String paramName = AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT;
        List<DeviceDTO> list = getDeviceDTOList(paramName);

        for (DeviceDTO device : list) {
            if (device.getDeviceUuid().equalsIgnoreCase(deviceUuid)) {
                device.setLayerName(layerName);
                device.setPolicyPackage(policyPackage);
                continue;
            }
        }
        int returnCode =  commonUpdateSetting( paramName, list);
        return returnCode;
    }

    @Override
    public List<DeviceInfoVO> getUnselectedSameVendorNameDeviceByJson(String paramName) {
        List<List<DeviceDTO>> selectedDeviceList = getActiveStandbyDeviceDTOList(paramName);
        List<NodeEntity> nodeEntityList = getNodeList();
        List<DeviceInfoVO> deviceList = new ArrayList<>();

        for (NodeEntity nodeEntity : nodeEntityList) {
            if(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY.equals(paramName)){
                if (!nodeEntity.getType().equals("0")) {
                    logger.info(String.format("设备[%s](%s)为非防火墙类型，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                    continue;
                }
            }else if (AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY.equals(paramName)){
                if (!nodeEntity.getType().equals("0") && !nodeEntity.getType().equals("1")) {
                    logger.info(String.format("设备[%s](%s)为非防火墙类型和非路由交换设备，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                    continue;
                }
            }

            boolean existDevice = false;
            for(List<DeviceDTO> deviceDTOList : selectedDeviceList){
                for (DeviceDTO device : deviceDTOList) {
                    if (device.getDeviceUuid().equals(nodeEntity.getUuid())) {
                        logger.info(String.format("设备(%s)已在列表(%s)中...", nodeEntity.getUuid(), AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE));
                        existDevice = true;
                        break;
                    }
                }
            }
            if (existDevice) {
                continue;
            }

            //获取设备信息
            DeviceRO device = whaleService.getDeviceByUuid(nodeEntity.getUuid());
            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.error(String.format("根据设备uuid:%s,查询设备详情为空", nodeEntity.getUuid()));
                continue;
            }
            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            if (deviceData.getIsVsys() != null) {
                deviceInfoVO.setVsysName(deviceData.getVsysName());
                deviceInfoVO.setIsVsys(deviceData.getIsVsys());
            }


            deviceInfoVO.setUuid(nodeEntity.getUuid());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceList.add(deviceInfoVO);
        }
        return deviceList;
    }

    @Override
    public int addSameVendorNameDeviceToList(String paramName, List<String> list) {
        List<List<DeviceDTO>> deviceList = this.validateDevice(paramName);

        Set<String> vendorNameSet = new HashSet<>();
        List<DeviceDTO> deviceDTOList = new ArrayList<>();
        for(String deviceUuid : list) {
            DeviceDTO deviceDTO = new DeviceDTO();
            if(deviceList.contains(deviceUuid)) {
                logger.info(String.format("设备(%s)已在(%s)例外列表中", deviceUuid, paramName));
                continue;
            }
            NodeEntity addEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(null == addEntity){
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                continue;
            }
            vendorNameSet.add(addEntity.getVendorName());
            deviceDTO.setDeviceUuid(addEntity.getUuid());
            deviceDTO.setVendorName(addEntity.getVendorName());
            deviceDTO.setDeviceIp(addEntity.getIp());
            deviceDTOList.add(deviceDTO);
        }
        deviceList.add(deviceDTOList);
        if(vendorNameSet.size() > 1){
            return ReturnCode.SAME_VENDOR_NAME_ERROR;
        }

        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);

        boolean flag = this.updateDevice(entityList, deviceList);
        if(!flag){
            return ReturnCode.ADD_DEVICE_TO_LIST_FAILED;
        }
        String message = getBusinessType(paramName)+",添加主备双活设备成功";
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 获取主备双活设备列表
     * @param paramName
     * @return
     */
    private List<List<DeviceDTO>> getActiveStandbyDeviceDTOList(String paramName) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        List<List<DeviceDTO>> deviceDTOList = new ArrayList<>();
        if(list.size() == 0) {
            return deviceDTOList;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONObject jsonObject = JSONObject.parseObject(value);
        JSONArray jsonArray = jsonObject.getJSONArray("devices");
        for(int index =0; index < jsonArray.size(); index ++) {
            JSONArray deviceArray = jsonArray.getJSONArray(index);
            List<DeviceDTO> deviceDTO = new ArrayList<>();
            for(int index2 =0; index2 < deviceArray.size(); index2 ++) {
                JSONObject deviceObject = deviceArray.getJSONObject(index2);
                DeviceDTO device = JSONObject.toJavaObject(deviceObject, DeviceDTO.class);
                deviceDTO.add(device);
            }
            deviceDTOList.add(deviceDTO);
        }
        return deviceDTOList;
    }


    @Override
    public List<List<DeviceInfoVO>> selectedActiveStandbyList(String paramName){
        List<List<DeviceDTO>> deviceList = getActiveStandbyDeviceDTOList(paramName);
        List<List<DeviceInfoVO>> deviceInfoList = new ArrayList<>();
        for(List<DeviceDTO> deviceDTOList : deviceList){
            List<DeviceInfoVO> deviceInfoVOList = new ArrayList<>();
            for(DeviceDTO device:deviceDTOList) {
                String deviceUuid = device.getDeviceUuid();
                logger.info(String.format("获取设备(%s)节点信息", deviceUuid));
                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
                if (nodeEntity == null) {
                    logger.info("节点信息为空！");
                    continue;
                }
                DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
                deviceInfoVO.setManageIp(nodeEntity.getIp());
                deviceInfoVO.setName(nodeEntity.getDeviceName());
                deviceInfoVO.setVendorName(nodeEntity.getVendorName());
                deviceInfoVO.setUuid(deviceUuid);
                if(device.getRelatedRule() != null){
                    deviceInfoVO.setRelatedRule(device.getRelatedRule());
                }

                if (device.getGroupName() != null) {
                    deviceInfoVO.setGroupName(device.getGroupName());
                }
                deviceInfoVO.setLayerName(device.getLayerName());
                deviceInfoVO.setPolicyPackage(device.getPolicyPackage());
                deviceInfoVOList.add(deviceInfoVO);
            }
            deviceInfoList.add(deviceInfoVOList);
        }

        return deviceInfoList;
    }

    @Override
    public int removeActiveStandbyDevice(String paramName, String deviceUuids) {
        List<List<DeviceDTO>> deviceList = getActiveStandbyDeviceDTOList(paramName);
        boolean flag = this.removeDevice(deviceList,paramName, deviceUuids);
        if(!flag){
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-主备双活设置，删除设备成功");
        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 移除设备
     * @param deviceList
     * @param paramName
     * @param deviceUuids
     * @return
     */
    private boolean removeDevice(List<List<DeviceDTO>> deviceList, String paramName,String deviceUuids ){
        List<DeviceDTO> removeList = new ArrayList<>();
        String[] deviceUuidList = deviceUuids.split(",");
        for(String deviceUuid : deviceUuidList){
            for (List<DeviceDTO> deviceDTOList : deviceList) {
                for(DeviceDTO deviceDTO : deviceDTOList){
                    if (deviceDTO.getDeviceUuid().equals(deviceUuid)) {
                        removeList.add(deviceDTO);
                    }
                }
            }
        }
        deviceList.remove(removeList);

        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);
        return updateDevice(entityList, deviceList);
    }

    @Override
    public int removeDisasterRecoveryDevice(String paramName, String deviceUuids) {
        List<List<DeviceDTO>> deviceList = getActiveStandbyDeviceDTOList(paramName);
        boolean flag = this.removeDevice(deviceList,paramName, deviceUuids);
        if(!flag){
            return ReturnCode.REMOVE_UUID_FROM_LIST_FAILED;
        }
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), "修改高级设置-灾备设置，删除设备成功");
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public List<NodeEntity> getAnotherDeviceByIp(String paramName, String deviceIp) {
        List<NodeEntity> nodeEntityList = new ArrayList<>();
        List<List<DeviceInfoVO>> deviceInfoList = selectedActiveStandbyList(paramName);
        List<String> ipList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(deviceInfoList)){
            boolean contains = false;
            List<List<DeviceInfoVO>> ipListList = new ArrayList<>();
            for(List<DeviceInfoVO> dataList : deviceInfoList ){
                for(DeviceInfoVO deviceInfo : dataList ){
                    if(StringUtils.equals(deviceIp, deviceInfo.getManageIp())){
                        ipListList.add(dataList);
                        contains = true;
                    }
                }
                if(!contains){
                    ipListList.clear();
                }
            }
            if(contains){
                for (List<DeviceInfoVO> deviceInfoVOS : ipListList) {
                    for (DeviceInfoVO deviceInfoVO : deviceInfoVOS) {
                        ipList.add(deviceInfoVO.getManageIp());
                    }
                }
                if (ipList.size() > 1) {
                    ipList.remove(deviceIp);
                }
                // 查询新设备
                nodeEntityList = nodeMapper.getTheNodeByIpList(ipList);
            }
        }
        return nodeEntityList;
    }

    @Override
    public int addDisasterRecovery(String paramName, List<String> list) {
        List<List<DeviceDTO>> deviceList = this.validateDevice(paramName);

        List<DeviceDTO> deviceDTOList = new ArrayList<>();
        for(String deviceUuid : list) {
            DeviceDTO deviceDTO = new DeviceDTO();
            if(deviceList.contains(deviceUuid)) {
                logger.info(String.format("设备(%s)已在(%s)例外列表中", deviceUuid, paramName));
                continue;
            }
            NodeEntity addEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
            if(null == addEntity){
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                continue;
            }
            deviceDTO.setDeviceUuid(addEntity.getUuid());
            deviceDTO.setVendorName(addEntity.getVendorName());
            deviceDTO.setDeviceIp(addEntity.getIp());
            deviceDTOList.add(deviceDTO);
        }
        deviceList.add(deviceDTOList);

        List<AdvanceSettingsEntity> entityList = advanceSettingsMapper.selectByParamName(paramName);
        boolean flag = updateDevice(entityList, deviceList);
        if(!flag){
            return ReturnCode.ADD_DEVICE_TO_LIST_FAILED;
        }
        String message = getBusinessType(paramName)+",添加灾备设备成功";
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.POLICY_PUSH.getId(), message);

        return ReturnCode.POLICY_MSG_OK;
    }

    /**
     * 去除空设备
     * @param paramName
     * @return
     */
    private List<List<DeviceDTO>> validateDevice(String paramName){
        List<List<DeviceDTO>> deviceList = getActiveStandbyDeviceDTOList(paramName);
        List<DeviceDTO> removeList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(deviceList)){
            for(List<DeviceDTO> deviceDTOList : deviceList){
                for(DeviceDTO deviceDTO : deviceDTOList) {
                    NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceDTO.getDeviceUuid());
                    if(nodeEntity == null) {
                        logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceDTO.getDeviceUuid()));
                        removeList.add(deviceDTO);
                    }
                }
            }
        }

        deviceList.removeAll(removeList);
        return deviceList;
    }

    /**
     * 更新设置数据
     * @param entityList
     * @param deviceList
     * @return
     */
    private boolean updateDevice(List<AdvanceSettingsEntity> entityList, List<List<DeviceDTO>> deviceList){
        if(entityList == null || entityList.size() == 0 ) {
            logger.info("参数信息不存在，添加失败...");
            return false;
        }
        AdvanceSettingsEntity entity = entityList.get(0);
        JSONObject object = new JSONObject();
        String valueString = JSONObject.toJSONString(deviceList);
        JSONArray value = JSONArray.parseArray(valueString);
        object.put("devices", value);
        entity.setParamValue(object.toString());
        advanceSettingsMapper.updateByPrimaryKey(entity);
        return true;
    }

    @Override
    public int uploadPyFile(String paramName, MultipartFile file, String deviceUuid) {
        String importedName = file.getOriginalFilename();
        if(StringUtils.isBlank(deviceUuid)){
            return ReturnCode.DEVICE_NOT_EXIST;
        }
        String[] deviceUuids = deviceUuid.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<NodeEntity>  nodeEntities = new ArrayList<>();
        for (String itemDeviceUuid : deviceUuids){
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(itemDeviceUuid);
            if (nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                continue;
            }
            nodeEntities.add(nodeEntity);
        }


        try {

            String pathname = pyFileBasedir;
            String fileName = importedName.toLowerCase();
            if (fileName.contains(".")) {
                String name = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
                if (!"py".equals(name)) {
                    logger.info(String.format("上传文件格式错误,不是python文件", fileName));
                    return ReturnCode.FILE_FORMAT_ERROR;
                }
            }
            String filePath = pathname + fileName;
//            boolean isExist = TotemsFileUtils.existsFile(filePath);
//            if (isExist) {
//                logger.info(String.format("文件%s已经存在,上传文件失败", fileName));
//                return ReturnCode.FILE_IS_EXIST_ERROR;
//            }
            File newFile = new File(filePath);
            // 如果存在先删除
            TotemsFileUtils.deleteFile(filePath);
            // 再保存
            TotemsFileUtils.createDirectory(pathname);
            boolean success = TotemsFileUtils.createOrCoverFile(filePath, newFile);
            logger.info("创建文件是否成功{}", success);
            // 转存文件
            TotemsFileUtils.multipartFileToFile(newFile, file);

            List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
            if (list == null || list.size() == 0) {
                AdvanceSettingsEntity entity = buildAdvanceSettingParam(paramName, nodeEntities, fileName);
                advanceSettingsMapper.insert(entity);
            } else {
                AdvanceSettingsEntity entity = list.get(0);
                String value = entity.getParamValue();
                if(StringUtils.isBlank(value)){
                    AdvanceSettingsEntity updateEntity = buildAdvanceSettingParam(paramName, nodeEntities, fileName);
                    updateEntity.setId(entity.getId());
                    advanceSettingsMapper.updateByPrimaryKey(updateEntity);
                }else{
                    JSONArray jsonArray = JSONArray.parseArray(value);
                    // 先删除原有的
                    Iterator<Object> o = jsonArray.iterator();
                    while (o.hasNext()) {
                        JSONObject object = (JSONObject) o.next();
                        JSONArray itemJsonArray = object.getJSONArray("devices");
                        Iterator<Object> itemObj = itemJsonArray.iterator();
                        while (itemObj.hasNext()){
                            JSONObject itemObject = (JSONObject) itemObj.next();
                            String uuid = itemObject.getString("uuid");
                            if (deviceUuid.contains(uuid)) {
                                itemObj.remove();
                            }
                            if (0 == itemJsonArray.size()) {
                                o.remove();
                            }
                        }
                    }
                    // 再新增
                    JSONArray devicesArray = new JSONArray();
                    for (NodeEntity nodeEntity : nodeEntities){
                        JSONObject object = new JSONObject();
                        object.put("uuid", nodeEntity.getUuid());
                        object.put("name", nodeEntity.getDeviceName());
                        object.put("manageIp", nodeEntity.getIp());
                        devicesArray.add(object);
                    }

                    JSONObject updateObject = new JSONObject();
                    updateObject.put("devices",devicesArray);
                    updateObject.put("fileName", fileName);
                    jsonArray.add(updateObject);

                    entity.setParamValue(jsonArray.toJSONString());
                    advanceSettingsMapper.updateByPrimaryKey(entity);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ReturnCode.IMPORT_FAILED_INVALID_IMPORT_FILE;
        }
        return ReturnCode.POLICY_MSG_OK;
    }

    @Override
    public int addPushSpecialDevice(String paramName, String deviceUuid) {
        if(StringUtils.isBlank(deviceUuid)){
            return ReturnCode.DEVICE_NOT_EXIST;
        }
        String[] deviceUuids = deviceUuid.split(PolicyConstants.ADDRESS_SEPERATOR);
        List<NodeEntity>  nodeEntities = new ArrayList<>();
        for (String itemDeviceUuid : deviceUuids){
            NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(itemDeviceUuid);
            if (nodeEntity == null) {
                logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                continue;
            }
            nodeEntities.add(nodeEntity);
        }
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        if (list == null || list.size() == 0) {
            AdvanceSettingsEntity entity = buildAdvanceSettingParam(paramName, nodeEntities, null);
            advanceSettingsMapper.insert(entity);
        } else {
            AdvanceSettingsEntity entity = list.get(0);
            String value = entity.getParamValue();
            if (StringUtils.isBlank(value)) {
                AdvanceSettingsEntity updateEntity = buildAdvanceSettingParam(paramName, nodeEntities, null);
                updateEntity.setId(entity.getId());
                advanceSettingsMapper.updateByPrimaryKey(updateEntity);
            } else {
                JSONArray jsonArray = JSONArray.parseArray(value);
                // 先删除原有的
                Iterator<Object> o = jsonArray.iterator();
                while (o.hasNext()) {
                    JSONObject object = (JSONObject) o.next();
                    JSONArray itemJsonArray = object.getJSONArray("devices");
                    Iterator<Object> itemObj = itemJsonArray.iterator();
                    while (itemObj.hasNext()) {
                        JSONObject itemObject = (JSONObject) itemObj.next();
                        String uuid = itemObject.getString("uuid");
                        if (deviceUuid.contains(uuid)) {
                            itemObj.remove();
                        }
                        if (0 == itemJsonArray.size()) {
                            o.remove();
                        }
                    }
                }
                // 再新增
                JSONArray devicesArray = new JSONArray();
                for (NodeEntity nodeEntity : nodeEntities) {
                    JSONObject object = new JSONObject();
                    object.put("uuid", nodeEntity.getUuid());
                    object.put("name", nodeEntity.getDeviceName());
                    object.put("manageIp", nodeEntity.getIp());
                    devicesArray.add(object);
                }

                JSONObject updateObject = new JSONObject();
                updateObject.put("devices", devicesArray);
                updateObject.put("fileName", null);
                jsonArray.add(updateObject);

                entity.setParamValue(jsonArray.toJSONString());
                advanceSettingsMapper.updateByPrimaryKey(entity);
            }
        }
        return 0;
    }

    /**
     * 构建上传py文件参数
     * @param paramName
     * @param nodeEntities
     * @param fileName
     * @return
     */
    private AdvanceSettingsEntity buildAdvanceSettingParam(String paramName, List<NodeEntity> nodeEntities, String fileName) {
        JSONArray devicesArray = new JSONArray();

        for (NodeEntity nodeEntity : nodeEntities){
            JSONObject object = new JSONObject();
            object.put("uuid", nodeEntity.getUuid());
            object.put("name", nodeEntity.getDeviceName());
            object.put("manageIp", nodeEntity.getIp());
            devicesArray.add(object);
        }

        JSONObject object = new JSONObject();
        object.put("devices",devicesArray);
        object.put("fileName", fileName);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(object);
        AdvanceSettingsEntity entity = new AdvanceSettingsEntity();
        entity.setParamName(paramName);
        entity.setParamValue(jsonArray.toJSONString());
        return entity;
    }

    @Override
    public int deletePyFile(String paramName, String deviceUuid) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        if (list == null || list.size() == 0) {
            logger.info(String.format("查询下发特例高级设置:{}未查询到数据", paramName));
            return ReturnCode.POLICY_MSG_OK;
        }

        String pathname = pyFileBasedir;

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        JSONArray jsonArray = JSONArray.parseArray(value);

        Iterator<Object> itemObj = jsonArray.iterator();
        while (itemObj.hasNext()) {
            JSONObject object = (JSONObject) itemObj.next();


            JSONArray itemJsonArray = object.getJSONArray("devices");
            Iterator<Object> deviceItemObj = itemJsonArray.iterator();
            while (deviceItemObj.hasNext()) {
                JSONObject itemObject = (JSONObject) deviceItemObj.next();
                String uuid = itemObject.getString("uuid");
                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(uuid);
                if (nodeEntity == null) {
                    logger.info(String.format("设备(%s)已被删除,从列表移除...", uuid));
                    continue;
                }
                if(deviceUuid.contains(uuid)){
                    deviceItemObj.remove();
                }
                if (0 == itemJsonArray.size()) {
                    String existFileName = object.getString("fileName");
                    String filePath = pathname + existFileName;
                    // 如果存在先删除
                    TotemsFileUtils.deleteFile(filePath);
                    itemObj.remove();
                }
            }
        }
        entity.setParamValue(jsonArray.size() == 0 ? null : jsonArray.toJSONString());
        advanceSettingsMapper.updateByPrimaryKey(entity);
        return ReturnCode.POLICY_MSG_OK;
    }


    @Override
    public List<DeviceInfoVO> getPushFileDeviceList(String paramName) {
        List<String> existDeviceUuidList = getStandardDeviceLists(paramName);
        List<NodeEntity> nodeEntityList = getNodeList();
        List<DeviceInfoVO> deviceList = new ArrayList<>();

        for(NodeEntity nodeEntity:nodeEntityList) {
            if(!nodeEntity.getType().equals("0")) {
                logger.info(String.format("设备[%s](%s)为非防火墙类型，不加入列表...", nodeEntity.getDeviceName(), nodeEntity.getUuid()));
                continue;
            }
            if(existDeviceUuidList.contains(nodeEntity.getUuid())) {
                logger.info(String.format("设备(%s)已在列表(%s)中...", nodeEntity.getUuid(), AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD));
                continue;
            }

            //获取设备信息
            DeviceRO device = whaleService.getDeviceByUuid(nodeEntity.getUuid());
            if (device == null || device.getData() == null || device.getData().size() == 0) {
                logger.error(String.format("根据设备uuid:%s,查询设备详情为空", nodeEntity.getUuid()));
                continue;
            }
            //从设备信息中获取设备是否为虚设备信息
            DeviceDataRO deviceData = device.getData().get(0);
            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
            if (deviceData.getIsVsys() != null) {
                deviceInfoVO.setVsysName(deviceData.getVsysName());
                deviceInfoVO.setIsVsys(deviceData.getIsVsys());
            }

            deviceInfoVO.setUuid(nodeEntity.getUuid());
            deviceInfoVO.setVendorName(nodeEntity.getVendorName());
            deviceInfoVO.setName(nodeEntity.getDeviceName());
            deviceInfoVO.setManageIp(nodeEntity.getIp());
            deviceList.add(deviceInfoVO);
        }
        return deviceList;
    }


    /**
     * 获取标准json串中以uuid命名的设备uuid集合
     * @param paramName
     * @return
     */
    private List<String> getStandardDeviceLists(String paramName) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        List<String> device = new ArrayList<>();
        if (list.size() == 0) {
            return device;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        if (StringUtils.isBlank(value)) {
            return device;
        }
        JSONArray jsonArray = JSONObject.parseArray(value);
        for (int index = 0; index < jsonArray.size(); index++) {
            JSONObject deviceObject = jsonArray.getJSONObject(index);

            JSONArray itemJsonArray = deviceObject.getJSONArray("devices");
            Iterator<Object> itemObj = itemJsonArray.iterator();
            while (itemObj.hasNext()) {
                JSONObject itemObject = (JSONObject) itemObj.next();
                String uuid = itemObject.getString("uuid");
                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(uuid);
                if (nodeEntity == null) {
                    logger.info(String.format("设备(%s)已被删除,从列表移除...", uuid));
                    continue;
                }
                device.add(uuid);
            }
        }
        return device;
    }



    @Override
    public List<DeviceInfoAndBusinessVO> queryAllPushFile(String paramName) {


        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        List<DeviceInfoAndBusinessVO> resultDevice = new ArrayList<>();

        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        JSONArray jsonArray = JSONObject.parseArray(value);
        for(int index =0; index < jsonArray.size(); index ++) {
            JSONObject itemObject = jsonArray.getJSONObject(index);
            JSONArray devicesJsonArray = itemObject.getJSONArray("devices");

            List<DeviceInfoVO> deviceDTOList = new ArrayList<>();

            for(int index2 =0; index2 < devicesJsonArray.size(); index2 ++) {
                JSONObject deviceObject = devicesJsonArray.getJSONObject(index2);
                String deviceUuid = deviceObject.getString("uuid");
                NodeEntity nodeEntity = nodeMapper.getTheNodeByUuid(deviceUuid);
                if(nodeEntity == null) {
                    logger.info(String.format("设备(%s)已被删除,从列表移除...", deviceUuid));
                    continue;
                }

                DeviceInfoVO device = JSONObject.toJavaObject(deviceObject, DeviceInfoVO.class);
                deviceDTOList.add(device);
            }
            DeviceInfoAndBusinessVO deviceInfoAndBusinessVO = new DeviceInfoAndBusinessVO();
            deviceInfoAndBusinessVO.setDeviceInfoVOList(deviceDTOList);
            deviceInfoAndBusinessVO.setFileName(itemObject.getString("fileName"));
            resultDevice.add(deviceInfoAndBusinessVO);
        }
        return resultDevice;
    }

    @Override
    public String getPythonFileName(String paramName, String currentIp) {
        List<AdvanceSettingsEntity> list = advanceSettingsMapper.selectByParamName(paramName);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        AdvanceSettingsEntity entity = list.get(0);
        String value = entity.getParamValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        JSONArray jsonArray = JSONArray.parseArray(value);

        Iterator<Object> o = jsonArray.iterator();
        while (o.hasNext()) {
            JSONObject object = (JSONObject) o.next();
            if (object.containsKey("devices")){
                JSONArray itemJsonArray = object.getJSONArray("devices");
                Iterator<Object> itemObj = itemJsonArray.iterator();
                while (itemObj.hasNext()) {
                    JSONObject itemObject = (JSONObject) itemObj.next();
                    String manageIp = itemObject.getString("manageIp");
                    if (StringUtils.isBlank(manageIp)) {
                        continue;
                    }
                    if (manageIp.equals(currentIp)) {
                        return object.getString("fileName");
                    }
                }
            }
        }
        return null;
    }
}
