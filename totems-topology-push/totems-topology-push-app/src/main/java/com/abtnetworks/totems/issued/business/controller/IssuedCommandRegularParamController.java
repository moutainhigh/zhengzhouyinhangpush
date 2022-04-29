package com.abtnetworks.totems.issued.business.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.issued.business.entity.PushCommandRegularParamEntity;
import com.abtnetworks.totems.issued.business.service.CommandRegularParamService;
import com.abtnetworks.totems.issued.dto.AddCommandRegularParamDTO;
import com.abtnetworks.totems.issued.dto.CommandRegularParamPageDTO;
import com.abtnetworks.totems.issued.dto.CommandRegularUpdateDTO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
@Api(value = "策略下发命令集收集参数管理")
@RestController
@RequestMapping(value = "${startPath}/command/reg")
public class IssuedCommandRegularParamController extends BaseController {
    /****/
    @Resource
    CommandRegularParamService commandRegularParamService;
    /**
     * 记录日志
     **/
    @Resource
    LogClientSimple logClientSimple;

    /**
     * 收集型号的命令参数
     *
     * @param addCommandRegularParamDTO
     * @return
     */
    @ApiOperation("收集型号的命令参数")
    @PostMapping("/insert")
    public ReturnResult insertCommandRegParam(AddCommandRegularParamDTO addCommandRegularParamDTO, Authentication auth) {

        try {
            logger.info("收集型号的命令参数接口START{}", JSONObject.toJSONString(addCommandRegularParamDTO));
            if (addCommandRegularParamDTO.getPromptRegCommand().length() > 255) {
                return new ReturnResult(ReturnResult.FAIL_CODE, "命令终止符长度需小于255");
            }
            if (StringUtils.isNotEmpty(addCommandRegularParamDTO.getPromptErrorInfo()) && addCommandRegularParamDTO.getPromptErrorInfo().length() > 6144) {
                return new ReturnResult(ReturnResult.FAIL_CODE, "提示正则错误匹配命令长度最大不超过6144");
            }
            PushCommandRegularParamEntity pushCommandRegularParamEntity = new PushCommandRegularParamEntity();
            BeanUtils.copyProperties(addCommandRegularParamDTO, pushCommandRegularParamEntity);
            pushCommandRegularParamEntity.setUpdateEmp(auth.getName());
            pushCommandRegularParamEntity.setCreateEmp(auth.getName());
            int count = commandRegularParamService.addCommandRegularParam(pushCommandRegularParamEntity);
            if (count > 0) {
                logClientSimple.addBusinessLog("info", "8", "仿真开通-反馈管理添加厂商为" + addCommandRegularParamDTO.getVendorName() + "的型号" + addCommandRegularParamDTO.getModelNumber() + "成功");

            } else {
                logClientSimple.addBusinessLog("warm", "8", "仿真开通-反馈管理添加厂商为" + addCommandRegularParamDTO.getVendorName() + "的型号" + addCommandRegularParamDTO.getModelNumber() + "失败");

            }
            logger.info("收集型号的命令参数接口END{}", count);
            return ReturnResult.SUCCESS;
        } catch (DuplicateKeyException e) {
            logger.error("违反唯一索引约束，请重新选择已存在进行修改", e);
            return new ReturnResult(-1, "您输入的设备型号已经存在，可直接前往修改！");
        } catch (Exception e) {
            logger.error("系统异常", e);
            return ReturnResult.FAIL;
        }
    }

    /**
     * 收集型号的命令参数
     *
     * @param id
     * @return ReturnResult
     */
    @ApiOperation("删除id对应的型号的命令参数")
    @PostMapping("/delete")
    public ReturnResult deleteCommandRegParamById(String id, String modelNumber) {
        try {
            logger.info("删除id对应的型号的命令参数接口START{}", id);
            ReturnResult returnResult = new ReturnResult();
            if (StringUtils.isEmpty(id)) {
                returnResult.setCode(-2);
                returnResult.setMsg("参数为空");
            }
            int count = commandRegularParamService.deleteCommandRegParamById(id);
            if (count > 0) {
                logClientSimple.addBusinessLog("info", "8", "仿真开通-反馈管理删除型号为" + modelNumber + "成功");

            } else {
                logClientSimple.addBusinessLog("warm", "8", "仿真开通-反馈管理删除型号为" + id + "失败");

            }
            logger.debug("删除id对应的型号的命令参数接口END{}", count);
            return ReturnResult.SUCCESS;
        } catch (Exception e) {
            logger.error("系统异常", e);
            return ReturnResult.FAIL;
        }
    }


    /**
     * 收集型号的命令参数
     *
     * @param commandRegularUpdateDTO
     * @return ReturnResult
     */
    @ApiOperation("修改对应的型号的命令参数")
    @PostMapping("/update")
    public ReturnResult updateCommandRegularParamById(CommandRegularUpdateDTO commandRegularUpdateDTO, Authentication auth) {
        try {
            logger.info("修改对应的型号的命令参数接口START{}", JSONObject.toJSONString(commandRegularUpdateDTO));
            if (StringUtils.isNotEmpty(commandRegularUpdateDTO.getPromptErrorInfo()) && commandRegularUpdateDTO.getPromptErrorInfo().length() > 6144) {
                return new ReturnResult(ReturnResult.FAIL_CODE, "提示正则错误匹配命令长度最大不超过6144");
            }
            if (commandRegularUpdateDTO.getPromptRegCommand().length() > 255) {
                return new ReturnResult(ReturnResult.FAIL_CODE, "命令终止符长度需小于255");
            }

            commandRegularUpdateDTO.setUpdateEmp(auth.getName());
            int count = commandRegularParamService.updateCommandRegularParamById(commandRegularUpdateDTO);
            if (count > 0) {
                logClientSimple.addBusinessLog("info", "8", "仿真开通-反馈管理修改了的型号" + commandRegularUpdateDTO.getModelNumber() + "成功");

            } else {
                logClientSimple.addBusinessLog("warm", "8", "仿真开通-反馈管理修改了的型号" + commandRegularUpdateDTO.getModelNumber() + "失败");

            }
            logger.debug("修改对应的型号的命令参数接口END{}", count);
            return ReturnResult.SUCCESS;
        } catch (Exception e) {
            logger.error("系统异常", e);
            return ReturnResult.FAIL;
        }
    }

    /**
     * 收集型号的命令参数
     *
     * @param commandRegularParamPageDTO
     * @return ReturnResult
     */
    @ApiOperation("查询对应的型号的命令参数")
    @PostMapping("/list")
    public ReturnResult listCommandRegularParamForPage(CommandRegularParamPageDTO commandRegularParamPageDTO) {
        ReturnResult returnResult = new ReturnResult();
        try {
            logger.info("修改对应的型号的命令参数接口START入参{}", JSONObject.toJSONString(commandRegularParamPageDTO));
            PageInfo<PushCommandRegularParamEntity> paramEntityPageInfo = commandRegularParamService.getCommandRegularParamList(commandRegularParamPageDTO);
            logger.info("修改对应的型号的命令参数接口END返回参数{}", JSONObject.toJSONString(paramEntityPageInfo));
            returnResult.setContent(paramEntityPageInfo);
            returnResult.setCode(ReturnResult.SUCCESS_CODE);

        } catch (Exception e) {
            logger.error("系统异常", e);
            returnResult.setCode(ReturnResult.FAIL_CODE);
            returnResult.setMsg("系统异常");

        }
        return returnResult;
    }
}
