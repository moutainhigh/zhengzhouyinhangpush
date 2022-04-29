package com.abtnetworks.totems.auto.controller;

import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
import com.abtnetworks.totems.auto.service.PushProtectNetworkConfigService;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigSearchVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigVO;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @desc    防护网段配置
 * @author liuchanghao
 * @date 2021-06-10 09:26
 */
@Api(tags="防护网段配置")
@RestController
@RequestMapping(value="/protect/network")
public class PushProtectNetworkConfigController extends BaseController {

    private static Logger logger = Logger.getLogger(PushProtectNetworkConfigController.class);

    @Autowired
    private PushProtectNetworkConfigService pushProtectNetworkConfigService;


    @ApiOperation(value = "新建或编辑防护网段配置", httpMethod = "POST")
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public ReturnT addOrUpdate(@RequestBody ProtectNetworkConfigVO protectNetworkConfigVO) {
        try{
            return pushProtectNetworkConfigService.addOrUpdateConfig(protectNetworkConfigVO);
        }catch (Exception e) {
            logger.error("新增或编辑防护网段配置异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "新增或编辑防护网段配置异常");
        }
    }

    @ApiOperation(value = "删除防护网段配置", httpMethod = "POST")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ReturnT delete(@RequestBody ProtectNetworkConfigVO protectNetworkConfigVO) {
        try{
            return pushProtectNetworkConfigService.delete(protectNetworkConfigVO);
        }catch (Exception e) {
            logger.error("删除防护网段配置异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "删除防护网段配置异常");
        }
    }

    @ApiOperation(value = "分页查询护网段配置", httpMethod = "POST")
    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public ReturnT select(@RequestBody ProtectNetworkConfigSearchVO vo) {
        try{
            PageInfo<ProtectNetworkConfigEntity> pageInfoList = pushProtectNetworkConfigService.findList(vo, vo.getPage(), vo.getLimit());
            return new ReturnT(pageInfoList);
        }catch (Exception e) {
            logger.error("分页查询防护网段配置异常，异常原因：", e);
            return new ReturnT(ReturnT.FAIL_CODE, "分页查询防护网段配置异常");
        }
    }

}
