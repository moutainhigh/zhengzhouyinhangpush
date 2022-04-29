package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalScenesEntity;
import com.abtnetworks.totems.disposal.entity.DisposalScenesNodeEntity;
import com.abtnetworks.totems.disposal.service.DisposalScenesService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 17:29 2019/11/11
 */
@Api(value = "应急处置>>场景管理")
@RestController
@RequestMapping(value = "${startPath}/disposal/scenes")
public class DisposalScenesController extends BaseController {

    @Autowired
    private DisposalScenesService disposalScenesService;

    /**
     * 新增，修改
     */
    @ApiOperation("场景管理保存or修改，传参JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "deviceJson", value = "设备信息String类型的JSON", required = false, dataType = "String")
    })
    @PostMapping("/edit")
    public ReturnT<String> saveOrUpdate(Authentication authentication, @RequestBody DisposalScenesEntity disposalScenes){
        try {
            // valid
            if (disposalScenes == null || AliStringUtils.isEmpty(disposalScenes.getDeviceJson())) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失:deviceJson or name or remarks！");
            }
            List<DisposalScenesNodeEntity> scenesNodeEntityList = JSONObject.parseArray(disposalScenes.getDeviceJson(), DisposalScenesNodeEntity.class);
            for (DisposalScenesNodeEntity scenesNodeEntity : scenesNodeEntityList) {
                if (scenesNodeEntity == null || !AliStringUtils.areNotEmpty(scenesNodeEntity.getDeviceUuid(), scenesNodeEntity.getDeviceName())) {
                    return new ReturnT<String>(ReturnT.FAIL_CODE, "deviceJson 序列化对象时，部分数据为空，请检查！");
                }
            }
            disposalScenes.setCreateUser(authentication.getName());
            return disposalScenesService.saveOrUpdate(disposalScenes, scenesNodeEntityList);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 删除
     */
    @ApiOperation("场景管理删除")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "主键id", required = true, dataType = "Integer")
    })
    @PostMapping("/delete")
    public ReturnT<String> delete(Authentication authentication, int id){
        //查询
        DisposalScenesEntity scenesEntity = disposalScenesService.getById(id);
        if(scenesEntity == null){
            return ReturnT.FAIL;
        }
        ReturnT<String> returnT = disposalScenesService.delete(id);
        if (authentication != null) {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), authentication.getName() + " 删除场景" + scenesEntity.getName() + "成功！");
        } else {
            logger.error("场景管理删除场景，获取用户凭证失败！");
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), "场景管理删除场景"+scenesEntity.getName()+"，获取用户凭证失败！");
        }
        return returnT;
    }

    /**
     * 查询 get By Id
     */
    @ApiOperation("场景管理编辑，查询场景信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "uuid", value = "场景uuid", required = true, dataType = "String")
    })
    @PostMapping("/getByUUId")
    public ReturnT getByUUId(String uuid){
        try {
            disposalScenesService.clearRubbish();
            DisposalScenesEntity scenesEntity = disposalScenesService.getByUUId(uuid);
            List<DisposalScenesDTO> scenesDTOList = disposalScenesService.findByScenesUuid(uuid);
            Map<String, Object> map = new HashMap<>();
            map.put("scenes", scenesEntity);
            map.put("scenesNodeList", scenesDTOList);
            return new ReturnT(map);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 分页查询
     */
    @ApiOperation("场景管理List列表，查询传参JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "page", value = "页数", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "limit", value = "每页条数", required = false, dataType = "Integer")
    })
    @PostMapping("/pageList")
    public ReturnT pageList(Authentication authentication, @RequestBody DisposalScenesEntity disposalScenes) {
        try {
            PageInfo<DisposalScenesEntity> pageInfoList = disposalScenesService.findList(disposalScenes,
                    disposalScenes.getPage(), disposalScenes.getLimit());
            ReturnT returnT = new ReturnT(pageInfoList);
            return returnT;
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }

    }

}

