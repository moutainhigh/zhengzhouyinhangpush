package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalHandleListDTO;
import com.abtnetworks.totems.disposal.dto.DisposalJoinScenesDTO;
import com.abtnetworks.totems.disposal.entity.DisposalHandleEntity;
import com.abtnetworks.totems.disposal.service.DisposalHandleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 10:24 2019/11/12
 */
@Api(tags = {"协作管理"})
@RestController
@RequestMapping(value = "${startPath}/disposal/handle")
public class DisposalHandleController extends BaseController {

    @Autowired
    private DisposalHandleService disposalHandleService;

    @ApiOperation(value = "协作测试添加", httpMethod = "POST", notes = "", nickname = "鲁薇")
    @PostMapping("/insert")
    public ReturnT<String> insert(String jsonStr){
        return disposalHandleService.insert(jsonStr);
    }

    /**
     * 删除
     */
/*    @PostMapping("/delete")
    public ReturnT<String> delete(int id){
        return disposalHandleService.delete(id);
    }*/

    /**
     * 更新
     */
/*    @PostMapping("/update")
    public ReturnT<String> update(DisposalHandleEntity disposalHandle){
        return disposalHandleService.update(disposalHandle);
    }*/



    /**
     * 查询 get By Id
     */
    @PostMapping("/getById")
    public DisposalHandleEntity getById(Long id){
        return disposalHandleService.getById(id);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value = "协作列表", httpMethod = "POST", notes = "", nickname = "鲁薇")
    @PostMapping("/pageList")
    public ResultRO<List<DisposalHandleListDTO>> pageList(
            @ApiParam(name = "category", value = "类别：0策略、1路由", required = false, defaultValue = "0") @RequestParam(required = false)  Integer category,
            @ApiParam(name = "type", value = "任务类型：1场景、2黑IP、3路径", required = false, defaultValue = "0") @RequestParam(required = false)  Integer type,
            @ApiParam(name = "status", value = "状态：1未处置、2已处置、3自动处置", required = false, defaultValue = "1") @RequestParam(required = false)  Integer status,
            @ApiParam(name = "content", value = "内容", required = false, defaultValue = "192.168") @RequestParam(required = false)  String content,
            @ApiParam(name = "callbackFlag", value = "是否回滚, true回滚,false下发", required = false, defaultValue = "") @RequestParam(required = false)  Boolean callbackFlag,
            @ApiParam(name = "currentPage", value = "当前页", required = true) @RequestParam(defaultValue = "0") int currentPage,
            @ApiParam(name = "pageSize", value = "每页显示记录条数", required = true)  @RequestParam(defaultValue = "10") int pageSize) {
        return disposalHandleService.findList(category, type, status, content, callbackFlag, currentPage, pageSize);
    }

    @ApiOperation(value = "生成处置工单", httpMethod = "POST", notes = "", nickname = "鲁薇")
    @PostMapping("/create-order")
    public ResultRO createOrder(Authentication authentication,  @ApiParam(name = "id", value = "协作单ID", required = true) @RequestParam(defaultValue = "0") Long id) {
        String auditUser = "";
        if(authentication != null){
            auditUser = authentication.getName();
        }
        return disposalHandleService.createHandle(auditUser, id);
    }

    @ApiOperation(value = "生成处置工单-新建场景", httpMethod = "POST", notes = "", nickname = "鲁薇")
    @PostMapping("/join-scenes")
    public ResultRO joinScenes(Authentication authentication, @RequestBody DisposalJoinScenesDTO dto) {
        String auditUser = "";
        if (authentication != null) {
            auditUser = authentication.getName();
        }
        return disposalHandleService.joinScenes(auditUser, dto.getId(), dto.getUuidList());
    }

}

