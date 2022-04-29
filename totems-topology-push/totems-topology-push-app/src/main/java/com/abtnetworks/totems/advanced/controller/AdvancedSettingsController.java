package com.abtnetworks.totems.advanced.controller;

import com.abtnetworks.totems.advanced.dto.PushTimeLockDTO;
import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.advanced.vo.DeviceInfoAndBusinessVO;
import com.abtnetworks.totems.advanced.vo.DeviceInfoVO;
import com.abtnetworks.totems.advanced.vo.DeviceListPageVO;
import com.abtnetworks.totems.advanced.vo.DeviceListVO;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.TotemsReturnT;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.VendorEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value="高级设置")
@RestController
@RequestMapping(value="/advanced/")
public class AdvancedSettingsController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(AdvancedSettingsController.class);

    private static final String DEVICE_LIST_SEPERATOR = ",";

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @ApiOperation(value = "设置下发时间锁",httpMethod = "POST")
    @PostMapping("setPushTimeLock")
    public JSONObject setPushTimeLock(@RequestBody PushTimeLockDTO pushTimeLockDTO){
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_PUSH_TIME_LOCK,JSON.toJSONString(pushTimeLockDTO));
        return getReturnJSON(rc);
    }

    @ApiOperation("获取下发时间锁")
    @PostMapping("getPushTimeLock")
    public JSONObject getPushTimeLock(){
        String paramValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_PUSH_TIME_LOCK);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CONFIG_PUSH_TIME_LOCK,paramValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("获取是否创建对象设置")
    @PostMapping("createobject")
    public JSONObject getCreateObjectSetting() {
        String createObjectValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT, createObjectValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("设置是否创建对象")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="添加策略下发任务", required=true, dataType="String")
    })
    @PostMapping("setcreateobject")
    public JSONObject setCreateObjectSetting(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_OBJECT, value);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取是否新建策略设置")
    @PostMapping("createrule")
    public JSONObject getCreateRuleSetting() {
        String createRuleValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_RULES);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CREATE_RULES, createRuleValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("设置是否新建策略")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="添加策略下发任务", required=true, dataType="String")
    })
    @PostMapping("setcreaterule")
    public JSONObject setCreateRuleSetting(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CREATE_RULES, value);
        return getReturnJSON(rc);
    }


    @ApiOperation("获取移动到某条之前的策略的列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("movebeforedevicelist")
    public JSONObject getMoveBeforeDeviceList(int page, int psize){
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getMovePolicyDeviceList(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加设备到移动策略到某一条之前列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addmovebeforedevicelist")
    public JSONObject addMoveBeforeDeviceList(@RequestParam String devices,@RequestParam String connectType){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addMovePolicyDeviceForBeforeOrAfter(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE, deviceUuidList,connectType);

        return getReturnJSON(rc);
    }

    @ApiOperation("从设备到移动策略到某一条之前列表中删除")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="connectType", value="连接类型", required=true, dataType = "String")

    })
    @PostMapping("removefrommovebeforedevicelist")
    public JSONObject removeFromMoveBeforeDeviceList(@RequestParam String device,@RequestParam String connectType) {
        int rc = advancedSettingService.removeMovePolicyDeviceForbeforeAndAfter(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE, device,connectType);
        return getReturnJSON(rc);
    }

    @ApiOperation("设置移动到某条策略之前的策略名称")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="policy", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="connectType", value="连接方式", required=false, dataType = "String")
    })
    @PostMapping("setmovebeforedevicepolicy")
    public JSONObject setmovebeforedevicepolicy(@RequestParam String device, @RequestParam String policy,@RequestParam String connectType) {
        int rc = advancedSettingService.setMovePolicyDevicePolicy(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE, device, policy,connectType);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取移动到某条之后的策略的列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("moveafterdevicelist")
    public JSONObject getMoveAfterDeviceList(int page, int psize){
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getMovePolicyDeviceList(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加设备到移动策略到某一条之后列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addmoveafeterdevicelist")
    public JSONObject addMoveAfterDeviceList(@RequestParam String devices,@RequestParam String connectType){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addMovePolicyDeviceForBeforeOrAfter(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER, deviceUuidList,connectType);

        return getReturnJSON(rc);
    }

    @ApiOperation("从设备到移动策略到某一条之后列表中删除")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="connectType", value="连接类型", required=true, dataType = "String")

    })
    @PostMapping("removefrommoveafterdevicelist")
    public JSONObject removeFromMoveAfterDeviceList(@RequestParam String device,@RequestParam String connectType) {
        int rc = advancedSettingService.removeMovePolicyDeviceForbeforeAndAfter(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER, device,connectType);
        return getReturnJSON(rc);
    }

    @ApiOperation("设置移动到某条策略之后的策略名称")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="policy", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="connectType", value="连接方式", required=false, dataType = "String")


    })
    @PostMapping("setmoveafterdevicepolicy")
    public JSONObject setmoveafterdevicepolicy(@RequestParam String device, @RequestParam String policy,@RequestParam String connectType) {
        int rc = advancedSettingService.setMovePolicyDevicePolicy(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER, device, policy,connectType);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取移动策略到第一条设备的例外设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("movetopexceptionlist")
    public JSONObject getMoveTopExceptionDeviceList(int page, int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getDeviceSingList(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("获取移动策略已经配置的移动到冲突策略前设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("moveBeforeConflictList")
    public JSONObject getMoveBeforeConflictList(int page, int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getDeviceSingList(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("获取未设置移动策略到第一条例外设备列表")
    @PostMapping("movetopdevicelist")
    public JSONObject getMoveTopDeviceList(@RequestParam String connectType) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedMovePolicyDeviceList(connectType);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList);
        JSONObject jsonObject = new JSONObject();
        for(String vendorName:deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加设备到移动策略到第一条里外设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addmovetopexceptionlist")
    public JSONObject addMoveTopExceptionList(@RequestParam String devices,@RequestParam String connectType){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addDeviceToListForNotMove(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP, deviceUuidList,connectType);

        return getReturnJSON(rc);
    }

    @ApiOperation("从移动策略到第一条设备列表中移除设备")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="connectType", value="连接类型", required=true, dataType = "String")

    })
    @PostMapping("removefrommovetopexceptionlist")
    public JSONObject removeFromMoveTopExceptionList(@RequestParam String device,@RequestParam String connectType) {
        int rc = advancedSettingService.removeDeviceSingFromList(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP, device,connectType);
        return getReturnJSON(rc);
    }

    @ApiOperation("添加移动冲突策略前配置")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addBeforeConflictPolicylist")
    public JSONObject addBeforeConflictPolicylist(@RequestParam String devices,@RequestParam String connectType){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addDeviceToListForNotMove(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE, deviceUuidList,connectType);

        return getReturnJSON(rc);
    }

    @ApiOperation("删除移动冲突策略前配置")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID列表", required=true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name="connectType", value="连接类型", required=true, dataType = "String")

    })
    @PostMapping("removeBeforeConflictPolicylist")
    public JSONObject removeBeforeConflictPolicylist(@RequestParam String device,@RequestParam String connectType) {
        int rc = advancedSettingService.removeDeviceSingFromList(AdvancedSettingsConstants.PARAM_NAME_MOVE_BEFORECONFLICT_RULE, device,connectType);
        return getReturnJSON(rc);
    }



    @ApiOperation("获取指定源域的设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("getconfigsrczonedevicelist")
    public JSONObject getConfigSrcZoneDeviceList(int page, int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getDeviceList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("获取指定目的域的设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("getconfigdstzonedevicelist")
    public JSONObject getConfigDstZoneDeviceList(int page, int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getDeviceList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("获取不指定域的设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("getconfignozonedevicelist")
    public JSONObject getConfigNoZoneDeviceList(int page, int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getDeviceList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加到指定源域设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addconfigsrczonedevicelist")
    public JSONObject addConfigSrcZoneDeviceList(@RequestParam String devices){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addDeviceToList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE, deviceUuidList);

        return getReturnJSON(rc);
    }

    @ApiOperation("添加到指定目的域设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addconfigdstzonedevicelist")
    public JSONObject addConfigDstZoneDeviceList(@RequestParam String devices){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addDeviceToList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE, deviceUuidList);

        return getReturnJSON(rc);
    }

    @ApiOperation("添加不指定域设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addconfignozonedevicelist")
    public JSONObject addConfigNoZoneDeviceList(@RequestParam String devices){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addDeviceToList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE, deviceUuidList);

        return getReturnJSON(rc);
    }

    @ApiOperation("获取未指定域设备列表")
    @PostMapping("getconfigdefaultzonedevicelist")
    public JSONObject getConfigDefaultZoneDeviceList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedZoneDeviceList();
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList);
        JSONObject jsonObject = new JSONObject();
        for(String vendorName:deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("从指定源域设备列表中移除设备")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String")
    })
    @PostMapping("removefromconfigsrczonedevicelist")
    public JSONObject removeFromConfigSrcZoneDeviceList(@RequestParam String device) {
        int rc = advancedSettingService.removeDeviceFromList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_SRC_ZONE, device);
        return getReturnJSON(rc);
    }

    @ApiOperation("从指定源域设备列表中移除设备")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String")
    })
    @PostMapping("removefromconfigdstzonedevicelist")
    public JSONObject removeFromConfigDstZoneDeviceList(@RequestParam String device) {
        int rc = advancedSettingService.removeDeviceFromList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_DST_ZONE, device);
        return getReturnJSON(rc);
    }

    @ApiOperation("从不指定域设备列表中移除设备")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String")
    })
    @PostMapping("removefromconfignozonedevicelist")
    public JSONObject removeFromConfigNoZoneDeviceList(@RequestParam String device) {
        int rc = advancedSettingService.removeDeviceFromList(AdvancedSettingsConstants.PARAM_NAME_CONFIG_NO_ZONE, device);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取挂载out的设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="page", value="页数", required=true, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name="psize", value="每页条数", required=true, dataType = "Integer")
    })
    @PostMapping("getciscoacloutdevicelist")
    public JSONObject getCiscoACLOutDeviceList(int page, int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getDeviceList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("获取未设置挂载out设备列表")
    @PostMapping("getciscodevicelist")
    public JSONObject getCiscoDeviceList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedDeviceList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList, "思科");
        JSONObject jsonObject = new JSONObject();
        for(String vendorName:deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("从挂载out的设备列表中移除设备")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="device", value="设备UUID", required=true, dataType = "String")
    })
    @PostMapping("removeciscoacloutdevicelist")
    public JSONObject removeFromCiscoACLOutDeviceList(@RequestParam String device) {
        int rc = advancedSettingService.removeDeviceFromList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE, device);
        return getReturnJSON(rc);
    }

    @ApiOperation("添加挂载out的设备列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="devices", value="设备UUID列表", required=true, dataType = "String")
    })
    @PostMapping("addciscoacloutdevicelist")
    public JSONObject addCiscoACLOutDeviceList(@RequestParam String devices){
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addDeviceToList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE, deviceUuidList);

        return getReturnJSON(rc);
    }



    @ApiOperation("new 获取是否进行策略检查设置")
    @PostMapping("checkrule")
    public JSONObject getCheckRuleSetting() {
        String createRuleValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CHECK_RULE);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CHECK_RULE, createRuleValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("new 设置是否策略检查")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="是否策略检查", required=true, dataType="String")
    })
    @PostMapping("setcheckrule")
    public JSONObject setCheckRuleSetting(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CHECK_RULE, value);
        return getReturnJSON(rc);
    }

    @ApiOperation("new 设置是否山石FW回滚高级设置 ")
    @PostMapping("hillstonerollbacktype")
    public JSONObject hillstonerollbacktype() {
        String createRuleValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE);
        JSONObject object = new JSONObject();
        if (StringUtils.isEmpty(createRuleValue)){
            createRuleValue = "0";
        }
        object.put(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE, createRuleValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("new 山石FW回滚高级设置 ")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="山石FW回滚高级设置 0:name,1:id", required=true, dataType="String")
    })
    @PostMapping("sethillstonerollbacktype")
    public JSONObject setRollbackType(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_ROLLBACK_TYPE, value);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取设置juniper是全局地址还是安全域地址来生成命令行高级设置 ")
    @PostMapping("juniperaddresstype")
    public JSONObject juniperaddresstype() {
        String createRuleValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_GLOBLE_OR_SECURITY);
        JSONObject object = new JSONObject();
        if (StringUtils.isEmpty(createRuleValue)){
            createRuleValue = "0";
        }
        object.put(AdvancedSettingsConstants.PARAM_NAME_GLOBLE_OR_SECURITY, createRuleValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("juniper是全局地址还是安全域地址来生成命令行高级设置 ")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="juniper地址对象高级设置 0:安全域地址,1:全局地址", required=true, dataType="String")
    })
    @PostMapping("setjuniperaddresstype")
    public JSONObject setjuniperaddresstype(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_GLOBLE_OR_SECURITY, value);
        return getReturnJSON(rc);
    }


    @ApiOperation("获取是否风险分析设置")
    @PostMapping("checkrisk")
    public JSONObject getCheckRiskSetting() {
        String createRuleValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CHECK_RISK);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CHECK_RISK, createRuleValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("设置是否风险分析")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="是否策略检查", required=true, dataType="String")
    })
    @PostMapping("setcheckrisk")
    public JSONObject setCheckRiskSetting(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CHECK_RISK, value);
        return getReturnJSON(rc);
    }

    @ApiOperation("new 获取是否使用现有对象设置")
    @PostMapping("usecurrentobject")
    public JSONObject getUseCurrentObjectSetting() {
        String useCurrentObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_OBJECT);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_OBJECT, useCurrentObject);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("new 设置是否使用现有对象")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="是否策略检查", required=true, dataType="String")
    })
    @PostMapping("setusecurrentobject")
    public JSONObject setUseCurrentObjectSetting(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_OBJECT, value);
        return getReturnJSON(rc);
    }

    @ApiOperation("new 获取是否使用现有地址对象设置")
    @PostMapping("useaddressobject")
    public JSONObject getUseAddressObjectSetting() {
        String useCurrentObject = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT, useCurrentObject);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("new 设置是否使用现有地址对象")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="是否策略检查", required=true, dataType="String")
    })
    @PostMapping("setuseaddressobject")
    public JSONObject setUseAddressObjectSetting(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_CONFIG_USE_CURRENT_ADDRESS_OBJECT, value);
        return getReturnJSON(rc);
    }

    /**
     * 获取设备列表分页
     * @param list 设备列表
     * @param page 页数
     * @param psize 每页数量
     * @return 设备列表分页对象
     */
    private DeviceListPageVO getDeviceListPage(List<DeviceInfoVO> list, int page, int psize) {
        int startNo = (page-1) * psize;
        int endNo = page*psize;

        List<DeviceInfoVO> deviceInfoList = new ArrayList<>();
        if(list.size() >  startNo){
            for(int index = startNo; index < endNo; index ++ ) {
                if(index < list.size()) {
                    deviceInfoList.add(list.get(index));
                }
            }
        }
        DeviceListPageVO pageVO = new DeviceListPageVO();
        pageVO.setTotal(list.size());
        pageVO.setList(deviceInfoList);
        return pageVO;
    }

    /**
     * 格式化设备列表信息为前端所需格式
     * @param deviceInfoList 设备列表
     * @param vendorName 按照设备类型过滤，只选择某个厂商设备。若为空则选择所有厂商
     * @return 格式化后设备数据
     */
    private Map<String, DeviceListVO>  formatDeviceList(List<DeviceInfoVO> deviceInfoList, String vendorName){
        Map<String, DeviceListVO> deviceInfoMap = new HashMap<>();
        for(DeviceInfoVO deviceInfo:deviceInfoList) {
            if(vendorName!=null) {
                if(!deviceInfo.getVendorName().equals(vendorName)) {
                    continue;
                }
            }
            if(deviceInfoMap.containsKey(deviceInfo.getVendorName())) {
                DeviceListVO deviceListVO = deviceInfoMap.get(deviceInfo.getVendorName());
                deviceListVO.getData().add(deviceInfo);
                int total = deviceListVO.getTotal();
                deviceListVO.setTotal(total+1);
            } else {
                DeviceListVO deviceListVO = new DeviceListVO();
                List<DeviceInfoVO> list = new ArrayList<>();
                list.add(deviceInfo);
                deviceListVO.setData(list);
                int total = deviceListVO.getTotal();
                deviceListVO.setTotal(total+1);
                deviceInfoMap.put(deviceInfo.getVendorName(), deviceListVO);
            }
        }

        return deviceInfoMap;
    }

    private Map<String, DeviceListVO>  formatDeviceList(List<DeviceInfoVO> deviceInfoList){
        return formatDeviceList(deviceInfoList, null);
    }

    @ApiOperation("获取天融信设备列表")
    @PostMapping("get-topsec-device-list")
    public JSONObject getTopsecDeviceList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedDeviceByJson(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList, VendorEnum.TOPSEC.getName());
        JSONObject jsonObject = new JSONObject();
        for (String vendorName : deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加天融信设备")
    @PostMapping("add-topsec-device")
    public JSONObject addTopsecDeviceList(@ApiParam(name = "devices", required = true, value = "多个设备uuid,逗号分隔") @RequestParam String devices) {
        if (AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for (String deviceUuid : deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME, deviceUuidList,null);

        return getReturnJSON(rc);
    }

    @ApiOperation("获取已选择的天融信设备列表")
    @PostMapping("get-selected-topsec-device-list")
    public JSONObject addTopsecDeviceList(@ApiParam(name = "page", required = true, value = "当前页") int page,
                                          @ApiParam(name = "psize", required = true, value = "每页显示记录条数") int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getMovePolicyDeviceList(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("从天融信分组设备列表中移除设备")
    @PostMapping("remove-topsec-device")
    public JSONObject removeFromTopsecDeviceList(@ApiParam(name = "device", required = true, value = "设备uuid") @RequestParam String device) {
        int rc = advancedSettingService.removeMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_TOPSEC_GROUP_NAME, device);
        return getReturnJSON(rc);
    }

    @ApiOperation("设置天融信设备分组名称")
    @PostMapping("add-topsec-group-name")
    public JSONObject addTopSecGroupName(@ApiParam(name = "device", required = true, value = "设备uuid") @RequestParam String device,
                                         @ApiParam(name = "groupName", required = true, value = "组名称") @RequestParam String groupName) {

        int rc = advancedSettingService.addTopSecGroupName(device, groupName);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取checkPoint设备列表")
    @PostMapping("get-checkPoint-device-list")
    public JSONObject getCheckPointDeviceList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedDeviceByJson(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList, VendorEnum.CHECKPOINT.getName());
        JSONObject jsonObject = new JSONObject();
        for (String vendorName : deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加checkPoint设备")
    @PostMapping("add-checkPoint-device")
    public JSONObject addCheckPointDeviceList(@ApiParam(name = "devices", required = true, value = "多个设备uuid,逗号分隔") @RequestParam String devices) {
        if (StringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for (String deviceUuid : deviceList) {
            deviceUuidList.add(deviceUuid);
        }

        int rc = advancedSettingService.addMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT, deviceUuidList,null);

        return getReturnJSON(rc);
    }

    @ApiOperation("获取已选择的checkPoint的")
    @PostMapping("get-selected-checkPoint-device-list")
    public JSONObject addCheckPointDeviceList(@ApiParam(name = "page", required = true, value = "当前页") int page,
                                          @ApiParam(name = "psize", required = true, value = "每页显示记录条数") int psize) {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getMovePolicyDeviceList(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT);

        DeviceListPageVO pageVO = getDeviceListPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("从checkPoint分组设备列表中移除设备")
    @PostMapping("remove-checkPoint-device")
    public JSONObject removeFromCheckPointDeviceList(@ApiParam(name = "device", required = true, value = "设备uuid") @RequestParam String device) {
        int rc = advancedSettingService.removeMovePolicyDevice(AdvancedSettingsConstants.PARAM_NAME_CHECK_POINT, device);
        return getReturnJSON(rc);
    }

    @ApiOperation("设置checkPoint设备分组名称")
    @PostMapping("add-checkPoint-layer-package")
    public JSONObject addCheckPoint(@ApiParam(name = "device", required = true, value = "设备uuid") @RequestParam String device,
                                         @ApiParam(name = "layerName", required = true, value = "分层名") @RequestParam String layerName,
    @ApiParam(name = "policyPackage", required = true, value = "包名") @RequestParam String policyPackage) {

        int rc = advancedSettingService.addCheckPointLayerPackage(device, layerName,policyPackage);
        return getReturnJSON(rc);
    }


    @ApiOperation("主备双活设置列表")
    @PostMapping("get-active-standby-list")
    public JSONObject getActiveStandbyList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedSameVendorNameDeviceByJson(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList);
        JSONObject jsonObject = new JSONObject();
        for (String vendorName : deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加主备双活设备")
    @PostMapping("add-active-standby")
    public JSONObject addActiveStandby(@ApiParam(name = "devices", required = true, value = "多个设备uuid,逗号分隔") @RequestParam String devices) {
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }
        // 主备双活设备选且只能选择2个相同厂商下的设备
        int rightNum = 2;
        if(deviceUuidList.size() != rightNum ){
            return getReturnJSON(ReturnCode.SAME_VENDOR_NAME_ERROR);
        }

        int rc = advancedSettingService.addSameVendorNameDeviceToList(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY, deviceUuidList);

        return getReturnJSON(rc);
    }

    @ApiOperation("获取已选择的主备双活设备列表")
    @PostMapping("get-selected-active-standby-list")
    public JSONObject selectedActiveStandbyList(int page, int psize) {
        List<List<DeviceInfoVO>> deviceInfoList = advancedSettingService.selectedActiveStandbyList(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY);

        DeviceListPageVO pageVO = getActiveStandbyPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("从主备双活设置中移除设备")
    @PostMapping("remove-active-standby-device")
    public JSONObject removeActiveStandbyDevice(@ApiParam(name = "device", required = true, value = "设备uuid") @RequestParam String device) {
        int rc = advancedSettingService.removeActiveStandbyDevice(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY, device);
        return getReturnJSON(rc);
    }


    @ApiOperation("灾备设置列表")
    @PostMapping("get-disaster-recovery-list")
    public JSONObject getDisasterRecoveryList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getUnselectedSameVendorNameDeviceByJson(AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList);
        JSONObject jsonObject = new JSONObject();
        for (String vendorName : deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("添加灾备设备")
    @PostMapping("add-disaster-recovery")
    public JSONObject addDisasterRecovery(@ApiParam(name = "devices", required = true, value = "多个设备uuid,逗号分隔") @RequestParam String devices) {
        if(AliStringUtils.isEmpty(devices)) {
            return getReturnJSON(ReturnCode.PLEASE_SELECT_A_DEVICE);
        }

        int rightNum = 2;
        String[] deviceList = devices.split(DEVICE_LIST_SEPERATOR);
        List<String> deviceUuidList = new ArrayList<>();
        for(String deviceUuid:deviceList) {
            deviceUuidList.add(deviceUuid);
        }
        if(deviceUuidList.size() < rightNum ){
            return getReturnJSON(ReturnCode.DEVICE_NUM_ERROR);
        }

        int rc = advancedSettingService.addDisasterRecovery(AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY, deviceUuidList);

        return getReturnJSON(rc);
    }

    @ApiOperation("获取已选择的灾备设备列表")
    @PostMapping("get-selected-disaster-recovery-list")
    public JSONObject selectedDisasterRecoveryList(int page, int psize) {
        List<List<DeviceInfoVO>> deviceInfoList = advancedSettingService.selectedActiveStandbyList(AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY);
        DeviceListPageVO pageVO = getActiveStandbyPage(deviceInfoList, page, psize);
        String jsonObjectString = JSONObject.toJSONString(pageVO);
        JSONObject jsonObject = JSONObject.parseObject(jsonObjectString);

        return getReturnJSON(ReturnCode.POLICY_MSG_OK, jsonObject);
    }

    @ApiOperation("从灾备设置中移除设备")
    @PostMapping("remove-disaster-recovery-device")
    public JSONObject removeDisasterRecoveryDevice(@ApiParam(name = "device", required = true, value = "设备uuid") @RequestParam String device) {
        int rc = advancedSettingService.removeDisasterRecoveryDevice(AdvancedSettingsConstants.PARAM_NAME_DISASTER_RECOVERY, device);
        return getReturnJSON(rc);
    }

    /**
     * 获取设备列表分页
     * @param list 设备列表
     * @param page 页数
     * @param psize 每页数量
     * @return 设备列表分页对象
     */
    private DeviceListPageVO getActiveStandbyPage(List<List<DeviceInfoVO>> list, int page, int psize) {
        List<List<DeviceInfoVO>> activeStandbyList = new ArrayList<>();
        DeviceListPageVO pageVO = new DeviceListPageVO();
        int endNo = page*psize;
        int startNo = (page-1) * psize;
        if(list.size() >  startNo){
            for(int index = startNo; index < endNo; index ++ ) {
                if(index < list.size()) {
                    activeStandbyList.add(list.get(index));
                }
            }
        }
        pageVO.setActiceStandbyList(activeStandbyList);
        pageVO.setTotal(list.size());
        return pageVO;
    }

    @ApiOperation("获取用户是否接收邮件")
    @PostMapping("getuserreceiveemail")
    public JSONObject getUserReceiveEmail() {
        String userReceiveEmailValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL, userReceiveEmailValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("设置用户是否接收邮件(0关闭，1开启)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="用户是否接收邮件", required=true, dataType="String")
    })
    @PostMapping("setuserreceiveemail")
    public JSONObject setUserReceiveEmail(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_USER_RECEIVE_EMAIL, value);
        return getReturnJSON(rc);
    }

    @ApiOperation("获取管理员邮箱")
    @PostMapping("getmanageremail")
    public JSONObject getManagerEmail() {
        String userReceiveEmailValue = advancedSettingService.getParamValue(AdvancedSettingsConstants.PARAM_NAME_MANAGER_EMAIL);
        JSONObject object = new JSONObject();
        object.put(AdvancedSettingsConstants.PARAM_NAME_MANAGER_EMAIL, userReceiveEmailValue);
        return getReturnJSON(ReturnCode.POLICY_MSG_OK, object);
    }

    @ApiOperation("设置管理员邮箱(多个以,隔开)")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name="value",value="管理员邮箱", required=true, dataType="String")
    })
    @PostMapping("setmanageremail")
    public JSONObject setManagerEmail(@RequestParam String value) {
        int rc = advancedSettingService.setParamValue(AdvancedSettingsConstants.PARAM_NAME_MANAGER_EMAIL, value);
        return getReturnJSON(rc);
    }



    @ApiOperation("下发特例设备列表")
    @PostMapping("/push-special-getPushFileDeviceList")
    public TotemsReturnT getPushFileDeviceList() {
        List<DeviceInfoVO> deviceInfoList = advancedSettingService.getPushFileDeviceList(AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD);
        Map<String, DeviceListVO> deviceMap = formatDeviceList(deviceInfoList);
        JSONObject jsonObject = new JSONObject();
        for (String vendorName : deviceMap.keySet()) {
            DeviceListVO deviceListVO = deviceMap.get(vendorName);
            jsonObject.put(vendorName, deviceListVO);
        }
        return new TotemsReturnT(jsonObject);
    }


    /**
     * 查询所有特例配置文件
     *
     * @return
     */
    @ApiOperation("查询所有特例配置文件")
    @PostMapping(value = "push-special-queryAllPushFile")
    public TotemsReturnT queryAllPushFile(int page, int psize) {
        List<DeviceInfoAndBusinessVO> deviceInfoVOS = advancedSettingService.queryAllPushFile(AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD);
        DeviceListPageVO deviceListPageVO = getDeviceListPageList(deviceInfoVOS, page, psize);
        return new TotemsReturnT(deviceListPageVO);
    }

    /**
     * 获取设备列表分页
     * @param list 设备列表
     * @param page 页数
     * @param psize 每页数量
     * @return 设备列表分页对象
     */
    private DeviceListPageVO getDeviceListPageList(List<DeviceInfoAndBusinessVO> list, int page, int psize) {
        List<DeviceInfoAndBusinessVO> dataList = new ArrayList<>();
        DeviceListPageVO pageVO = new DeviceListPageVO();
        int endNo = page*psize;
        int startNo = (page-1) * psize;
        if(list.size() >  startNo){
            for(int index = startNo; index < endNo; index ++ ) {
                if(index < list.size()) {
                    dataList.add(list.get(index));
                }
            }
        }
        pageVO.setDeviceList(dataList);
        pageVO.setTotal(list.size());
        return pageVO;
    }

    /**
     * 上传下发特例配置文件
     *
     * @param file       提交的文件
     * @param deviceUuid 设备uuid
     * @return
     */
    @ApiOperation("上传下发特例配置文件")
    @PostMapping(value = "push-special-uploadFile")
    public TotemsReturnT uploadFile(MultipartFile file, String deviceUuid) {
        validatorParam(deviceUuid);
        if (file.isEmpty()) {
            return TotemsReturnT.FAIL;
        }
        int rc = advancedSettingService.uploadPyFile(AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD, file, deviceUuid);
        return new TotemsReturnT(rc, ReturnCode.getMsg(rc));
    }

    /**
     * 新增下发特例设备数据
     *
     * @param deviceUuid 设备uuid
     * @return
     */
    @ApiOperation("新增下发特例设备数据")
    @PostMapping(value = "push-special-addDevice")
    public TotemsReturnT pushSpecialAddDevice(String deviceUuid) {
        validatorParam(deviceUuid);
        int rc = advancedSettingService.addPushSpecialDevice(AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD, deviceUuid);
        return new TotemsReturnT(rc, ReturnCode.getMsg(rc));
    }

    /**
     * 根据设备uuid删除下发特殊场景的py文件
     *
     * @param deviceUuid 设备uuid
     * @return
     */
    @ApiOperation("删除下发特殊场景文件")
    @PostMapping(value = "push-special-deleteFile")
    public TotemsReturnT deleteFile(String deviceUuid) {
        validatorParam(deviceUuid);
        int rc = advancedSettingService.deletePyFile(AdvancedSettingsConstants.PARAM_NAME_PYTHON_FILE_UPLOAD, deviceUuid);
        return new TotemsReturnT(rc, ReturnCode.getMsg(rc));
    }



}
