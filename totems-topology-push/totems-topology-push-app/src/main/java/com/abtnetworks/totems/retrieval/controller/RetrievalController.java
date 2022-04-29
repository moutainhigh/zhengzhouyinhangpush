package com.abtnetworks.totems.retrieval.controller;

import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.retrieval.dto.RetrievalParamDto;
import com.abtnetworks.totems.retrieval.service.RetrievalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author: wangxinghui
 * @Date: 2021/08/18
 */
@Api(value="全文检索：策略内容修改")
@RestController
@RequestMapping(value = "/retrieval/")
public class RetrievalController extends BaseController {

    @Autowired
    private RetrievalService retrievalService;

    @ApiOperation("修改策略命令行")
    @PostMapping("task/updatePolicyCommandline")
    public ReturnResult updatePolicyCommandline(@RequestBody RetrievalParamDto retrievalParamDto){
//        ReturnResult returnResult = new ReturnResult();
//        try {
//            if(retrievalParamDto!=null){
//
//                returnResult.setContent(retrievalService.getCommandline2(retrievalParamDto));
//
//            }
//            returnResult.setCode(ReturnResult.SUCCESS_CODE);
//        }catch (Exception e){
//            returnResult.setMsg("系统异常");
//            returnResult.setCode(ReturnResult.FAIL_CODE);
//        }
        return retrievalService.getCommandline2(retrievalParamDto);
    }

}
