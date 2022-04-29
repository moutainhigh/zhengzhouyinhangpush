package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.entity.DisposalBranchEntity;
import com.abtnetworks.totems.disposal.service.DisposalBranchService;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @Author hw
 * @Description
 * @Date 16:53 2019/11/11
 */
@Api(tags = {"下级单位管理"})
@RestController
@RequestMapping(value = "${startPath}/disposal/branch")
public class DisposalBranchController extends BaseController {

    @Autowired
    private DisposalBranchService disposalBranchService;

    @Value("${push.work.ip}")
    private String workIp;

    @KafkaListener(topics = "workQueue")
    public void insert(String content){
        logger.info("kafka 接收协作单位, 开始消费,content:{}", content);
        DisposalBranchEntity disposalBranch = JSONObject.parseObject(content, DisposalBranchEntity.class);
        logger.info("本单位IP:{}", workIp);
        if(disposalBranch.getIp().equals(workIp)){
            logger.info("本单位跳过处理");
            return;
        }

        if(disposalBranch.getIp().equals("127.0.0.1")){
            logger.info("总部跳过处理");
            return;
        }

        DisposalBranchEntity po = disposalBranchService.getByNameAndIp(disposalBranch.getName(), disposalBranch.getIp());
        if(po != null){
            logger.info("机构已存在，跳过处理");
            return;
        }

        disposalBranchService.insert(disposalBranch);
        logger.info("kafka 接收协作单位，消费完毕");
    }

    /**
     * 删除
     */
    @ApiOperation(value = "删除", httpMethod = "POST", notes = "", nickname = "鲁薇")
    @PostMapping("/delete")
    public ReturnT<String> delete(@ApiParam(name = "id", value = "下级单位ID", required = true, defaultValue = "0") @RequestParam Integer id){
        return disposalBranchService.delete(id);
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public ReturnT<String> update(DisposalBranchEntity disposalBranch){
        return disposalBranchService.update(disposalBranch);
    }

    /**
     * 查询 get By Id
     */
    @PostMapping("/getById")
    public DisposalBranchEntity getById(int id){
        return disposalBranchService.getById(id);
    }

    /**
     * 分页查询
     */
    @ApiOperation("下级单位List列表，传参JSON格式")
    @PostMapping("/pageList")
    public ReturnT pageList(@RequestParam(required = false, defaultValue = "0") int page,
                            @RequestParam(required = false, defaultValue = "10") int limit) {
        ReturnT returnT;
        try {
            PageInfo<DisposalBranchEntity> pageInfoList = disposalBranchService.findList(null, page, limit);
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            returnT = ReturnT.FAIL;
            logger.error("",e);
        }

        return returnT;
    }

}

