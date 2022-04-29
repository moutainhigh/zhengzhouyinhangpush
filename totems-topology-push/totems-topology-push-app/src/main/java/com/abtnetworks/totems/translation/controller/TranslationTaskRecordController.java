package com.abtnetworks.totems.translation.controller;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.translation.BaseRequest;
import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.enums.CommandLineTranslationStatus;
import com.abtnetworks.totems.translation.request.TranslationTaskRecordQueryRequest;
import com.abtnetworks.totems.translation.service.TranslationTaskRecordService;
import com.abtnetworks.totems.translation.vo.DeviceZoneInterfaceVO;
import com.abtnetworks.totems.translation.vo.TranslationTaskRecordVO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description 策略迁移信息表
 * @Version --
 * @Created by hw on '2021-01-12 10:38:35'.
 */
@Api(tags = "策略迁移Controller")
@RestController
@RequestMapping(value = "${startPath}/translation/taskRecord")
public class TranslationTaskRecordController extends BaseController {

    @Resource
    private TranslationTaskRecordService translationTaskRecordService;

    @Value("${virtual-directory.handler}")
    private String virtualDirectoryHandler;

    @Value("${virtual-directory.fileDir}")
    private String virtualDirectoryFileDir;

    @Resource
    WhaleManager whaleManager;

    /**
     * start 翻译
     */
    @ApiOperation("开始转换")
    @PostMapping("/startTranslation")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "策略迁移任务id", required = false, dataType = "Integer")
    })
    public ReturnT<String> startTranslation(@RequestParam Integer id, Authentication auth){
        if (id == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "id不能为空。");
        }

        try {
            TranslationTaskRecordEntity entity = translationTaskRecordService.getById(id);
            if(entity == null){
                return new ReturnT<String>(ReturnT.FAIL_CODE, "该条记录不存在，无法操作");
            }
            if(entity.getStatus().equalsIgnoreCase(CommandLineTranslationStatus.CONVERTING.getCode())){
                return new ReturnT<String>(ReturnT.FAIL_CODE, "正在转换中，请勿重复操作。");
            }
            return translationTaskRecordService.startTranslation(entity);
        } catch (Exception e) {
            logger.error("",e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 取消转换
     */
    @ApiOperation("取消转换")
    @PostMapping("/cancelTranslation")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "策略迁移任务id", required = false, dataType = "Integer")
    })
    public ReturnT<String> cancelTranslation(@RequestParam Integer id, Authentication auth){
        if (id == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "id不能为空。");
        }

        try {
            return translationTaskRecordService.cancelTranslation(id);
        } catch (Exception e) {
            logger.error("",e);
            return ReturnT.FAIL;
        }
    }

    @ApiOperation("获取策略转换进度条")
    @PostMapping("/getTranslationTaskProgress")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "策略迁移任务id", required = false, dataType = "Integer")
    })
    public ReturnT getTranslationTaskProgress(@RequestParam Integer id, Authentication auth){
        try {
            return translationTaskRecordService.getTranslationTaskProgress(id);
        } catch (Exception e) {
            logger.error("",e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 通过id 获取翻译后的配置
     */
    @ApiOperation("通过id 获取翻译后的配置")
    @PostMapping("/getCommandLineConfig")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "策略迁移任务id", required = false, dataType = "Integer")
    })
    public ReturnT<String> getCommandLineConfig(@RequestParam Integer id, Authentication auth){
        if (id == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "id不能为空。");
        }

        try {
            TranslationTaskRecordEntity entity = translationTaskRecordService.getById(id);
            String config = translationTaskRecordService.getCommandLineConfigByUUID(entity.getUuid());
            if (StringUtils.isNotBlank(config)) {
                return new ReturnT<>(config);
            } else {
                return new ReturnT<>("暂无数据。");
            }

        } catch (Exception e) {
            logger.error("",e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 新增
     */
    @ApiOperation("新增")
    @PostMapping("/insert")
    public ReturnT<String> insert(@RequestBody TranslationTaskRecordEntity entity, Authentication auth){

        if (entity == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        try {
            entity.setCreateUser(auth.getName());
            return translationTaskRecordService.insert(entity);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 删除
     */
    @ApiOperation("删除策略迁移任务")
    @PostMapping("/delete")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "策略迁移任务id", required = false, dataType = "Integer")
    })
    public ReturnT<String> delete(@RequestParam int id){
        try {
            return translationTaskRecordService.delete(id);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 删除
     */
    @ApiOperation("批量删除策略迁移任务")
    @PostMapping("/batchDelete")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "ids", value = "策略迁移任务id,逗号拼接", required = false, dataType = "String")
    })
    public ReturnT<String> batchDelete(@RequestParam String ids){
        if (StringUtils.isEmpty(ids)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "ids必要参数缺失");
        }
        try {
            return translationTaskRecordService.batchDelete(ids);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 更新
     */
    @ApiOperation("更新")
    @PostMapping("/update")
    public ReturnT<String> update(@RequestBody TranslationTaskRecordEntity entity){

        if (entity == null || entity.getId() == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }

        try {
            return translationTaskRecordService.update(entity);
        } catch (Exception e) {
            logger.error("",e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 查询 get By Id
     */
    @ApiOperation("查询 get By Id")
    @PostMapping("/getById")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "策略迁移任务id", required = false, dataType = "Integer")
    })
    public ReturnT getById(@RequestParam int id){
        TranslationTaskRecordEntity entity = null;
        try {
            entity = translationTaskRecordService.getById(id);
            return new ReturnT(entity);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 分页查询
     */
    @ApiOperation("分页查询")
    @PostMapping("/pageList")
    public ReturnT pageList(@RequestBody BaseRequest<TranslationTaskRecordQueryRequest> br) {
        // valid
        if (br == null || br.getVal() == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
        }
        ReturnT returnT;
        try {
            PageInfo<TranslationTaskRecordVO> pageInfo = translationTaskRecordService.findList(br.getVal(), br.getPage(), br.getLimit());
            returnT = new ReturnT(pageInfo);
        } catch (Exception e) {
            logger.error("", e);
            returnT = ReturnT.FAIL;
        }
        return returnT;

    }


    @ApiOperation("获取设备的域和接口对象")
    @PostMapping("/getDeviceZoneInterfaceRo")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "deviceUuid", value = "设备Uuid", required = true, dataType = "String")
    })
    public ReturnT getDeviceZoneInterface(@RequestParam String deviceUuid){
        try {
            List<Map<String,Object>> resultList = new ArrayList<>();
            DeviceRO deviceRO = whaleManager.getDeviceByUuid(deviceUuid);
            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            if(deviceDataRO == null || CollectionUtils.isEmpty(deviceRO.getData())){
                return new ReturnT<>(resultList);
            }

            List<DeviceZoneInterfaceVO> zoneList = new ArrayList<>();
            ZoneRO deviceZoneVO = whaleManager.getDeviceZoneVO(deviceUuid);
            List<ZoneDataRO> zoneData = deviceZoneVO.getData();
            // 思科无域信息
            if(!deviceDataRO.getManufacturer().contains("Cisco") && CollectionUtils.isNotEmpty(zoneData)){
                for (ZoneDataRO zoneDataRo : zoneData) {
                    if(StringUtils.isBlank(zoneDataRo.getName())){
                        continue;
                    }
                    DeviceZoneInterfaceVO deviceZoneInterfaceVO = new DeviceZoneInterfaceVO();
                    deviceZoneInterfaceVO.setName(zoneDataRo.getName());
                    deviceZoneInterfaceVO.setType(1);
                    deviceZoneInterfaceVO.setDeviceUuid(deviceUuid);
                    deviceZoneInterfaceVO.setZoneUuid(zoneDataRo.getUuid());
                    zoneList.add(deviceZoneInterfaceVO);
                }
                Map<String,Object> zoneMap = new HashMap<>();
                zoneMap.put("name","安全域");
                zoneMap.put("type",1);
                zoneMap.put("children",zoneList);
                resultList.add(zoneMap);
            }


            List<DeviceZoneInterfaceVO> interfaceList = new ArrayList<>();
            List<DeviceInterfaceRO> deviceInterfaces = deviceDataRO.getDeviceInterfaces();
            if(CollectionUtils.isNotEmpty(deviceInterfaces)){
                for (DeviceInterfaceRO deviceInterface : deviceInterfaces) {
                    DeviceZoneInterfaceVO deviceZoneInterfaceVO = new DeviceZoneInterfaceVO();
                    if(deviceDataRO.getManufacturer().contains("Cisco")){
                        if(StringUtils.isBlank(deviceInterface.getAlias())){
                            continue;
                        }
                        // 思科使用接口别名
                        deviceZoneInterfaceVO.setName(deviceInterface.getAlias());
                    } else {
                        if(StringUtils.isBlank(deviceInterface.getName())){
                            continue;
                        }
                        deviceZoneInterfaceVO.setName(deviceInterface.getName());
                    }
                    deviceZoneInterfaceVO.setType(2);
                    deviceZoneInterfaceVO.setDeviceUuid(deviceUuid);
                    interfaceList.add(deviceZoneInterfaceVO);
                }

                Map<String,Object> interfaceMap = new HashMap<>();
                interfaceMap.put("name","接口");
                interfaceMap.put("type",2);
                interfaceMap.put("children",interfaceList);
                resultList.add(interfaceMap);
            }

            return new ReturnT<>(resultList);
        } catch (Exception e) {
            logger.error("getDeviceZoneInterface error:",e);
            return ReturnT.FAIL;
        }
    }

}
