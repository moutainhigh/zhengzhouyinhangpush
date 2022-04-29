package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.PushConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.tools.excel.ImportExcel;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.CommonConfigParam;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.*;
import com.abtnetworks.totems.disposal.entity.*;
import com.abtnetworks.totems.disposal.enums.DisposalCategoryEnum;
import com.abtnetworks.totems.disposal.request.OrderSendCommandQueryRequest;
import com.abtnetworks.totems.disposal.service.*;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.dozermapper.core.Mapper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @Author hw
 * @Description
 * @Date 18:49 2019/11/11
 */
@Api(value = "应急处置>>封堵工单")
@RestController
@RequestMapping(value = "${startPath}/disposal/order")
public class DisposalOrderController extends BaseController {

    @Autowired
    private Mapper dozerMapper;

    @Value("${resourceHandler}")
    private String resourceHandler;

    @Value("${importRoutingOrderFileName}")
    private String importRoutingOrderFileName;

    @Value("${importPolicyOrderFileName}")
    private String importPolicyOrderFileName;

    @Autowired
    private DisposalOrderService disposalOrderService;

    @Autowired
    private DisposalOrderCenterService disposalOrderCenterService;

    @Autowired
    private DisposalTeamBranchService disposalTeamBranchService;

    @Autowired
    private DisposalCreateCommandLineRecordService createCommandLineRecordService;

    @Autowired
    private DisposalDeleteCommandLineRecordService deleteCommandLineRecordService;

    @Autowired
    private DisposalScenesService disposalScenesService;

    @ApiOperation("封堵工单，执行下发命令")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "centerUuid", value = "工单内容UUID", required = false, dataType = "String")
    })
    @PostMapping("/sendCommand")
    public ReturnT sendCommand(Authentication authentication, @RequestBody OrderSendCommandQueryRequest queryRequest) {
        try {
            if (queryRequest == null || queryRequest.getCenterUuidList() == null || queryRequest.getCenterUuidList().size() == 0) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
            }
            List<String> errorList = new ArrayList<>();
            for (String centerUuid : queryRequest.getCenterUuidList()) {
                DisposalOrderEntity orderEntity = disposalOrderService.getOrderEntityByCenterUuid(centerUuid);
                if (orderEntity != null) {
                    if (orderEntity.getStatus() == PushConstants.PUSH_INT_PUSH_RESULT_STATUS_DONE) {
                        DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(centerUuid);
                        errorList.add(orderDTO.getOrderNo()+" 已执行完成！");
                    } else if (orderEntity.getStatus().intValue() == 0 || orderEntity.getStatus().intValue() == 8) {
                        if (AliStringUtils.isEmpty(centerUuid)) {
                            return new ReturnT<String>(ReturnT.FAIL_CODE, "必要参数缺失");
                        }
                        //数据流id
                        String streamId = DateUtils.formatDate(new Date(), "yyyyMMddHHmmss")+"-"+IdGen.randomBase62(6);
                        //开始执行下发
                        disposalOrderService.startSendCommandTasks(streamId, centerUuid, authentication.getName());
                    } else {
                        errorList.add("参数：" + centerUuid + "错误，工单状态错误，无法进行下发！");
                    }
                } else {
                    errorList.add("参数："+centerUuid+"错误，未找到工单 或 已经删除！");
                }
            }
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    authentication.getName()+"执行封堵工单批量下发");
            return new ReturnT(errorList);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 新增，修改
     */
    @ApiOperation("封堵工单编辑修改，传参为JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "type", value = "工单类型：1手动、2黑IP、3路径", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "category", value = "分类：策略，路由", required = false, dataType = "Integer")
    })
    @PostMapping("/edit")
    public ReturnT<Map<String, String>> saveOrUpdate(@RequestBody DisposalOrderDTO orderDTO, Authentication authentication) {
        try {
            if (orderDTO == null || orderDTO.getCategory() == null || orderDTO.getType() == null || orderDTO.getAction() == null) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "必要参数缺失");
            }

            if (AliStringUtils.isEmpty(orderDTO.getOrderName()) || orderDTO.getOrderName().length() > 50) {
                return new ReturnT<>(ReturnT.FAIL_CODE, "工单名称不能为空或长度过长，请修改！");
            }

            if (orderDTO.getCategory() == 1) {
                List<String> listIp = InputValueUtils.ipConvert(orderDTO.getRoutingIp(), 256);
                if (listIp == null) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "黑洞路由IP格式错误或范围个数超过256个，请修改！");
                }
            }

            if (orderDTO.getType() == 1) {
                if (orderDTO.getScenesUuidArray() == null || orderDTO.getScenesUuidArray().length == 0) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "场景封堵>>请选择封堵场景！");
                }
            }

            /*参数说明:分类：0：策略，1：路由 / 工单类型：1手动、2黑IP、3路径
              逻辑说明:判断策略封堵的黑ip封堵，srcIp和dstIp 只能填写一个
                       判断策略封堵，场景封堵时，srcIp和dstIp 必填一项
            */
            if (orderDTO.getCategory() == 0 && orderDTO.getType() == 2) {
                if (StringUtils.isNoneBlank(orderDTO.getSrcIp(), orderDTO.getDstIp())) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "策略封堵>>黑IP自动封堵>>源IP和目的IP只能填写一个！");
                }
                if (StringUtils.isAllBlank(orderDTO.getSrcIp(), orderDTO.getDstIp())) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "策略封堵>>黑IP自动封堵>>源IP和目的IP必填一项！");
                }
            } else if (orderDTO.getCategory() == 0 && orderDTO.getType() == 1) {
                if (StringUtils.isAllBlank(orderDTO.getSrcIp(), orderDTO.getDstIp())) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "策略封堵>>场景封堵>>源IP和目的IP必填一项！");
                }
            }

            StringBuilder failureMsg = new StringBuilder();
            String ipType = null;
            if (orderDTO.getIpv6()) {
                ipType = ImportExcelVerUtils.IP_TYPE_IPV6;
            } else {
                ipType = ImportExcelVerUtils.IP_TYPE_IPV4;
            }
            /*校验输入的五元组数据 SrcIp DstIp Port*/
            //源地址
            if (StringUtils.isNoneBlank(orderDTO.getSrcIp())) {
                ResultRO<String> srcIpResult = ImportExcelVerUtils.checkIpByIpv46(orderDTO.getSrcIp(), "源地址", ipType);
                if (srcIpResult.getSuccess()) {
                    orderDTO.setSrcIp(srcIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(srcIpResult.getMessage())) {
                        failureMsg.append(srcIpResult.getMessage() + " <br/>");
                    }
                }
            }

            //目的地址
            if (StringUtils.isNoneBlank(orderDTO.getDstIp())) {
                ResultRO<String> dstIpResult = ImportExcelVerUtils.checkIpByIpv46(orderDTO.getDstIp(), "目的地址", ipType);
                if (dstIpResult.getSuccess()) {
                    orderDTO.setDstIp(dstIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(dstIpResult.getMessage())) {
                        failureMsg.append(dstIpResult.getMessage() + " <br/>");
                    }
                }
            }

            //服务
            if (StringUtils.isNotBlank(orderDTO.getServiceList())) {
                List<ServiceDTO> serviceList = ServiceDTOUtils.toList(orderDTO.getServiceList());
                if (serviceList == null) {
                    failureMsg.append("服务格式错误！只能为TCP，UDP，或者ICMP！" + " <br/>");
                }
                for (ServiceDTO service : serviceList) {
                    if (StringUtils.isNotBlank(service.getDstPorts())) {
                        //仅校验端口即可
                        service.setDstPorts(InputValueUtils.autoCorrectPorts(service.getDstPorts()));
                    }
                }
                orderDTO.setServiceList(ServiceDTOUtils.toString(serviceList));
            }

            //黑洞路由IP
            if (StringUtils.isNoneBlank(orderDTO.getRoutingIp())) {
                ResultRO<String> routingIpResult = ImportExcelVerUtils.checkIpByIpv46(orderDTO.getRoutingIp(), "黑洞路由IP", ipType);
                if (routingIpResult.getSuccess()) {
                    orderDTO.setRoutingIp(routingIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(routingIpResult.getMessage())) {
                        failureMsg.append(routingIpResult.getMessage() + " <br/>");
                    }
                }
            }

            String errorMsg = failureMsg.toString();

            //只要有异常信息，就返回，不做入库操作
            if (StringUtils.isNotBlank(errorMsg)) {
                return new ReturnT<>(ReturnT.FAIL_CODE, errorMsg);
            }

            if (authentication != null && authentication.getName() != null) {
                orderDTO.setCreateUser(authentication.getName());
            }

            /*参数说明:分类：0：策略，1：路由 / 工单类型：1手动、2黑IP、3路径
              逻辑说明:判断策略封堵的黑ip封堵，srcIp和dstIp 只能填写一个*/
            if (orderDTO.getCategory() == 0 && orderDTO.getType() == 2) {
                if (StringUtils.isBlank(orderDTO.getSrcIp()) && StringUtils.isNotBlank(orderDTO.getDstIp())) {
                    orderDTO.setSrcIp(null);
                }
                if (StringUtils.isBlank(orderDTO.getDstIp()) && StringUtils.isNotBlank(orderDTO.getSrcIp())) {
                    orderDTO.setDstIp(null);
                }
            } else {
                // 源ip 目的ip 判断ipv6 ipv4 赋值any
                if (AliStringUtils.isEmpty(orderDTO.getSrcIp())) {
                    if (orderDTO.getIpv6()) {
                        orderDTO.setSrcIp(PolicyConstants.IPV6_ANY);
                    } else {
                        orderDTO.setSrcIp(PolicyConstants.IPV4_ANY);
                    }
                }
                if (AliStringUtils.isEmpty(orderDTO.getDstIp())) {
                    if (orderDTO.getIpv6()) {
                        orderDTO.setDstIp(PolicyConstants.IPV6_ANY);
                    } else {
                        orderDTO.setDstIp(PolicyConstants.IPV4_ANY);
                    }
                }
            }

            ReturnT<Map<String, String>> returnT = disposalOrderService.saveOrUpdate(orderDTO);
            if (returnT.getCode() == ReturnT.SUCCESS_CODE && returnT.getData() != null) {
                Map<String, String> map = returnT.getData();
                if (map.containsKey("uuid")) {
                    String centerUuid = map.get("uuid");
                    disposalOrderService.threadGenerateCommand(orderDTO.getCreateUser(), centerUuid);
                }
            }

            return returnT;
        } catch (Exception e) {
            logger.error("", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "操作失败");
        }
    }

    /**
     * 删除
     */
    @ApiOperation("封堵工单删除")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "主键id", required = true, dataType = "Integer")
    })
    @PostMapping("/delete")
    public ReturnT<String> delete(Authentication authentication, Integer id){
        if (id == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "id必要参数缺失");
        }
        DisposalOrderEntity orderEntity = disposalOrderService.getById(id);
        DisposalOrderCenterEntity orderCenter = disposalOrderCenterService.getByUuid(orderEntity.getCenterUuid());

        ReturnT returnT = disposalOrderService.delete(id);
        String result = ReturnT.STR_MSG_SUCCESS.equals(returnT.getMsg())?"成功":"失败";
        if (authentication != null) {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    authentication.getName()+" 应急处置，封堵工单删除："+orderCenter.getOrderNo()+"，结果："+result);
        } else {
            logger.error("应急处置，封堵工单删除：获取用户凭证异常！");
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), "应急处置，封堵工单删除："+orderCenter.getOrderNo()
                    +"获取用户凭证失败！删除结果："+result);
        }

        return returnT;
    }

    /**
     * 批量删除
     */
    @ApiOperation("封堵工单批量删除")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "ids", value = "主键id", required = true, dataType = "String")
    })
    @PostMapping("/batchDelete")
    public ReturnT batchDelete(Authentication authentication, @RequestParam String ids){
        if (AliStringUtils.isEmpty(ids)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "ids必要参数缺失");
        }
        String logInfo = " 删除封堵工单：";
        if (authentication != null) {
            logInfo = authentication.getName() + logInfo;
        } else {
            logger.error("应急处置，封堵工单批量删除，获取用户凭证异常！");
        }
        Map<String, String> rMap = new HashMap<>();
        try {
            String[] arr = ids.split(",");
            for (String id : arr) {
                DisposalOrderEntity orderEntity = disposalOrderService.getById(Integer.valueOf(id));
                DisposalOrderCenterEntity orderCenter = disposalOrderCenterService.getByUuid(orderEntity.getCenterUuid());

                ReturnT<String> returnT = disposalOrderService.delete(Integer.valueOf(id));
                String result = ReturnT.STR_MSG_SUCCESS.equals(returnT.getMsg())?"成功":"失败";
                rMap.put(id, result);
                logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo+orderCenter.getOrderNo()+"，结果："+result);
            }
        } catch (Exception e) {
            logger.error("封堵工单批量删除", e);
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(), logInfo+"异常(失败)");
            return ReturnT.FAIL;
        }
        return new ReturnT(rMap);
    }

    /**
     * 查询 get Dto By centerUuid
     */
    @ApiOperation("查询封堵工单内容")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "centerUuid", value = "工单内容UUID", required = true, dataType = "String")
    })
    @PostMapping("/getByCenterUuid")
    public ReturnT getByCenterUuid(String centerUuid){
        try {
            DisposalOrderDTO orderDTO = disposalOrderService.getByCenterUuid(centerUuid);
            return new ReturnT(orderDTO);
        } catch (Exception e) {
            logger.error("", e);
            return ReturnT.FAIL;
        }
    }

    /**
     * 分页查询
     */
    @ApiOperation("封堵工单List列表，传参JSON格式")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "type", value = "工单类型：1手动、2黑IP、3路径", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "category", value = "分类：策略，路由", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "page", value = "页数", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "limit", value = "每页条数", required = false, dataType = "Integer")
    })
    @PostMapping("/pageList")
    @CrossOrigin
    public ReturnT pageList(@RequestBody DisposalOrderDTO disposalOrderDTO) {
        ReturnT returnT;
        try {
            PageInfo<DisposalOrderDTO> pageInfoList = disposalOrderService.findDtoList(disposalOrderDTO,
                    disposalOrderDTO.getPage(), disposalOrderDTO.getLimit());
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            returnT = ReturnT.FAIL;
            logger.error("",e);
        }

        return returnT;
    }


    /**
     * 分页查询
     */
    @ApiOperation("攻击链，风险路径查询封堵历史工单")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "type", value = "工单类型：1手动、2黑IP、3路径", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType="query", name = "category", value = "分类：策略，路由", required = false, dataType = "Integer")
    })
    @PostMapping("/findAttackChainDtoList")
    @CrossOrigin
    public ReturnT findAttackChainDtoList(@RequestBody AttackChainDisposalQueryOrderDTO queryOrderDTO) {
        ReturnT returnT;
        try {
            List<AttackChainDisposalOrderDTO> list = disposalOrderService.findAttackChainDtoList(queryOrderDTO);
            returnT = new ReturnT(list);
        } catch (Exception e) {
            returnT = ReturnT.FAIL;
            logger.error("",e);
        }

        return returnT;
    }


    /**
     * 封堵：设备下发的命令行记录查询
     */
    @ApiOperation("封堵：设备下发的命令行记录查询List，传参JSON格式")
    @PostMapping("/nodeCreateCommandLineRecordList")
    public ReturnT nodeCreateCommandLineRecordList(@RequestBody DisposalCreateCommandLineRecordEntity createCommandLineRecordEntity) {
        ReturnT returnT;
        try {
            PageInfo<DisposalNodeCommandLineRecordDTO> pageInfoList =
                    createCommandLineRecordService.findListByCenterUuidOrOrderNo(createCommandLineRecordEntity.getCenterUuid(),
                            createCommandLineRecordEntity.getOrderNo(), createCommandLineRecordEntity.getPage(), createCommandLineRecordEntity.getLimit());
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            returnT = ReturnT.FAIL;
            logger.error("",e);
        }

        return returnT;
    }

    /**
     * 回滚：设备下发的命令行记录查询
     */
    @ApiOperation("回滚：设备下发的命令行记录查询List，传参JSON格式")
    @PostMapping("/nodeDeleteCommandLineRecordList")
    public ReturnT nodeDeleteCommandLineRecordList(@RequestBody DisposalDeleteCommandLineRecordEntity deleteCommandLineRecordEntity) {
        ReturnT returnT;
        try {
            PageInfo<DisposalNodeCommandLineRecordDTO> pageInfoList =
                    deleteCommandLineRecordService.findListByCenterUuidOrOrderNo(deleteCommandLineRecordEntity.getCenterUuid(),
                            deleteCommandLineRecordEntity.getOrderNo(), deleteCommandLineRecordEntity.getPage(), deleteCommandLineRecordEntity.getLimit());
            returnT = new ReturnT(pageInfoList);
        } catch (Exception e) {
            returnT = ReturnT.FAIL;
            logger.error("",e);
        }

        return returnT;
    }


    @ApiOperation(value = "下载导入策略封堵工单Excel模板", httpMethod = "POST")
    @PostMapping(value = "/download-policy-import-template")
    public ResultRO<String> downloadPolicyImportTemplate() {
        ResultRO<String> resultRO = new ResultRO<>(true);
        resultRO.setData(resourceHandler.replace("**", "") + importPolicyOrderFileName);
        return resultRO;
    }

    @ApiOperation(value = "下载导入路由封堵工单Excel模板", httpMethod = "POST")
    @PostMapping(value = "/download-routing-import-template")
    public ResultRO<String> downloadRoutingImportTemplate() {
        ResultRO<String> resultRO = new ResultRO<>(true);
        resultRO.setData(resourceHandler.replace("**", "") + importRoutingOrderFileName);
        return resultRO;
    }

    @ApiOperation(value = "导入策略封堵工单Excel", httpMethod = "POST")
    @PostMapping(value = "/batch-policy-import")
    public ResultRO batchPolicyImport(MultipartFile file, Authentication auth){
        String userName = "";
        if(auth != null){
            userName = auth.getName();
        }

        ReturnT returnT = batchExcelDataImport(file, userName, 0);
        if (returnT.getCode() == ReturnT.SUCCESS_CODE) {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    auth.getName()+" 应急处置，导入策略封堵工单Excel：导入成功");
            return new ResultRO<>(true, "导入成功");
        } else {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    auth.getName()+" 应急处置，导入策略封堵工单Excel：导入失败");
            return new ResultRO(false, JSONObject.toJSONString(returnT.getData()==null?returnT.getMsg():returnT.getData()));
        }
    }

    @ApiOperation(value = "导入路由封堵工单Excel", httpMethod = "POST")
    @PostMapping(value = "/batch-routing-import")
    public ResultRO batchRoutingImport(MultipartFile file, Authentication auth){
        String userName = "";
        if(auth != null){
            userName = auth.getName();
        }

        ReturnT returnT = batchExcelDataImport(file, userName, 1);
        if (returnT.getCode() == ReturnT.SUCCESS_CODE) {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    auth.getName()+" 应急处置，导入路由封堵工单Excel：导入成功");
            return new ResultRO<>(true, "导入成功");
        } else {
            logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.PUSH_DISPOSAL.getId(),
                    auth.getName()+" 应急处置，导入路由封堵工单Excel：导入失败");
            return new ResultRO(false, JSONObject.toJSONString(returnT.getData()==null?returnT.getMsg():returnT.getData()));
        }
    }

    /**
     * 策略封堵，路由封堵工单Excel导入
     * @param file Excel文件
     * @param userName 用户名
     * @param category 0策略封堵，1路由封堵
     * @return
     */
    public ReturnT batchExcelDataImport(MultipartFile file, String userName, Integer category){
        logger.info("----------------------------导入策略，路由封堵工单Excel开始------------------------------");
        String errorMsg = "";

        try{
            if(file == null){
                return new ReturnT<String>(ReturnT.FAIL_CODE, "文件不能为空");
            }

            //校验后缀格式
            String fileName = file.getOriginalFilename();
            logger.info("导入上传的文件名称: " + fileName);
            String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            logger.info("导入的文件后缀,suffix:{}", suffix);
            if (!suffix.equals(".xls") && !suffix.equals(".xlsx")) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "文件格式错误，请使用标准模板上传");
            }

            StringBuilder failureMsg = new StringBuilder();
            //策略，路由封堵工单>>场景封堵导入
            ImportExcel scenesEi = new ImportExcel(file, 0, 0);
            //策略，路由封堵工单>>黑IP自动封堵导入
            ImportExcel evilIpEi = new ImportExcel(file, 0, 1);

            //读取sheet数据
            List<ImportOrderDTO> scenesList = new ArrayList<>();
            List<ImportOrderDTO> evilIpList = new ArrayList<>();
            if (category == 0) {
                List<ImportPolicyScenesOrderDTO> policyScenesList = scenesEi.getDataList(ImportPolicyScenesOrderDTO.class);
                dozerMapper.map(policyScenesList, scenesList);
                List<ImportPolicyEvilIpOrderDTO> policyEvilIpList = evilIpEi.getDataList(ImportPolicyEvilIpOrderDTO.class);
                dozerMapper.map(policyEvilIpList, evilIpList);
            } else if (category == 1) {
                List<ImportRoutingScenesOrderDTO> routingScenesList = scenesEi.getDataList(ImportRoutingScenesOrderDTO.class);
                dozerMapper.map(routingScenesList, scenesList);
                List<ImportRoutingEvilIpOrderDTO> routingEvilIpList = evilIpEi.getDataList(ImportRoutingEvilIpOrderDTO.class);
                dozerMapper.map(routingEvilIpList, evilIpList);
            }

            if ((scenesList == null || scenesList.isEmpty()) && (evilIpList == null || evilIpList.isEmpty())) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "文件无数据");
            }

            //获取库中已存在的工单名称
            Map<String,String> nameMap = getAllOrderName(category, null);
            //当前excel中的工单名称
            Map<String, String> currentNameMap = new HashMap<>();
            //获取库中已存在的场景名称
            Map<String,String> scenesMap = getAllScenesName();

            //校验 category 0策略封堵，1路由封堵 / type 工单类型：1手动（场景）、2黑IP、3路径
            List<DisposalOrderDTO> scenesDataList = checkListData(category, 1, scenesList, nameMap, currentNameMap, scenesMap, failureMsg, userName);
            List<DisposalOrderDTO> evilIpDataList = checkListData(category, 2, evilIpList, nameMap, currentNameMap, scenesMap, failureMsg, userName);
            errorMsg = failureMsg.toString();

            //只要有异常信息，就返回，不做入库操作
            if (StringUtils.isNotBlank(errorMsg)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, errorMsg);
            }

            //入库
            ReturnT<List<String>> scenesReturnT = disposalOrderService.batchSave(scenesDataList,userName);
            ReturnT<List<String>> evilIpReturnT = disposalOrderService.batchSave(evilIpDataList,userName);

            List<String> centerUuidList = new ArrayList<>();
            if (scenesReturnT.getCode() == ReturnT.SUCCESS_CODE && scenesReturnT.getData() != null) {
                centerUuidList.addAll(scenesReturnT.getData());
            }

            if (evilIpReturnT.getCode() == ReturnT.SUCCESS_CODE && evilIpReturnT.getData() != null) {
                centerUuidList.addAll(evilIpReturnT.getData());
            }

            //调用生成命令行
            for (String centerUuid : centerUuidList) {
                disposalOrderService.threadGenerateCommand(userName, centerUuid);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("场景封堵", scenesReturnT.getMsg());
            resultMap.put("黑IP自动封堵", evilIpReturnT.getMsg());

            return new ReturnT(resultMap);
        }catch (RuntimeException e) {
            errorMsg = "导入失败，请确保场景名称能正确关联，IP格式正确!";
            logger.error("导入任务列表失败：", e);
        } catch (Exception e) {
            errorMsg = "文件格式错误，请使用标准模板上传!";
            logger.error("导入任务列表失败：", e);
        }

        logger.info("----------------------------导入策略，路由封堵工单Excel结束------------------------------");
        return new ReturnT<String>(ReturnT.FAIL_CODE, errorMsg);
    }

    /**
     * 获取所以工单名称
     * @param category 分类 0:策略，1:路由
     * @param type 工单类型：1手动、2黑IP、3路径
     * @return
     */
    private Map<String, String> getAllOrderName(Integer category, Integer type){
        Map<String, String> map = new HashMap<>();
        List<DisposalOrderCenterEntity> listAll = disposalOrderCenterService.findListAll(category, type);
        if(listAll == null || listAll.size() == 0){
            return map;
        }

        for (DisposalOrderCenterEntity entity : listAll) {
            map.put(entity.getOrderName(), entity.getOrderName());
        }

        return map;
    }

    /**
     * 获取所以工单名称
     * @return
     */
    private Map<String, String> getAllScenesName(){
        Map<String, String> map = new HashMap<>();
        List<DisposalScenesEntity> listAll = disposalScenesService.findListAll();
        if(listAll == null || listAll.size() == 0){
            return map;
        }

        for (DisposalScenesEntity entity : listAll) {
            map.put(entity.getName(), entity.getUuid());
        }

        return map;
    }

    /**
     * 验证过滤数据
     * @param category 0策略封堵，1路由封堵
     * @param type 工单类型：1手动（场景）、2黑IP、3路径
     * @param batchList 数据list
     * @param nameMap 数据库工单名称Map集合
     * @param currentNameMap 当前Excel中的工单名称集合
     * @param scenesMap 场景名称，UUID 集合Map
     * @param failureMsg 错误返回失败消息
     * @param userName 用户名
     * @return
     */
    private List<DisposalOrderDTO> checkListData(Integer category, Integer type, List<ImportOrderDTO> batchList,
                                                 Map<String, String> nameMap, Map<String, String> currentNameMap,
                                                 Map<String, String> scenesMap, StringBuilder failureMsg, String userName){
        if(batchList == null ||  batchList.isEmpty()){
            return Collections.emptyList();
        }

        //日志前缀
        String logMsgPrefix = "";
        if (type == 1) {
            logMsgPrefix = "场景封堵：";
        } else if (type == 2) {
            logMsgPrefix = "黑IP自动封堵：";
        }

        int rowIndex = 2;
        for (ImportOrderDTO dto : batchList) {
            //过滤空行
            if (category == 0) {
                if (type == 1) {
                    if (StringUtils.isAllBlank(dto.getOrderName(), dto.getScenesNames(), dto.getStrIpv6(), dto.getSrcIp(), dto.getDstIp(),
                            dto.getServiceList(), dto.getAction(), dto.getRemarks())) {
                        rowIndex++;
                        continue;
                    }
                } else if (type == 2) {
                    if (StringUtils.isAllBlank(dto.getOrderName(), dto.getStrIpv6(), dto.getSrcIp(), dto.getDstIp(), dto.getAction(), dto.getRemarks())) {
                        rowIndex++;
                        continue;
                    }
                }
            } else if (category == 1) {
                if (type == 1) {
                    if (StringUtils.isAllBlank(dto.getOrderName(), dto.getScenesNames(), dto.getStrIpv6(), dto.getRoutingIp(), dto.getAction(), dto.getRemarks())) {
                        rowIndex++;
                        continue;
                    }
                } else if (type == 2) {
                    if (StringUtils.isAllBlank(dto.getOrderName(), dto.getStrIpv6(), dto.getRoutingIp(), dto.getAction(), dto.getRemarks())) {
                        rowIndex++;
                        continue;
                    }
                }
            }

            //判断工单名称是否重复
            if (StringUtils.isBlank(dto.getOrderName())) {
                failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，工单名称不能为空<br/>");
            } else if (StringUtils.isNoneBlank(dto.getOrderName())) {
                if (currentNameMap.get(dto.getOrderName()) != null) {
                    failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，工单名称" + dto.getOrderName() + "在模板中存在多条<br/>");
                } else if (nameMap.get(dto.getOrderName()) != null) {
                    failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，工单名称" + dto.getOrderName() + "在系统中已存在<br/>");
                }else{
                    currentNameMap.put(dto.getOrderName(), dto.getOrderName());
                }
            }

            //场景封堵，需要判断场景名称
            if (type == 1) {
                //判断场景名称是否存在
                if (StringUtils.isBlank(dto.getScenesNames())) {
                    failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，场景名称不能为空<br/>");
                } else if (StringUtils.isNoneBlank(dto.getScenesNames())) {
                    if (dto.getScenesNames().contains(",")) {
                        String[] scenesNameArray = dto.getScenesNames().split(",");
                        for (String scenesName : scenesNameArray) {
                            if (scenesMap.get(scenesName) == null) {
                                failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，场景名称" + scenesName + "在系统中不存在，请检查场景列表<br/>");
                            }
                        }
                    } else {
                        if (scenesMap.get(dto.getScenesNames()) == null) {
                            failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，场景名称" + dto.getScenesNames() + "在系统中不存在，请检查场景列表<br/>");
                        }
                    }
                }
            }

            //过滤非法数据
            if (category == 0) {
                /*开始验证>>策略封堵>>*/
                if (type == 1) {
                    /*场景封堵>>*/
                    if (!StringUtils.isNoneBlank(dto.getStrIpv6(), dto.getAction())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，IP地址类型、动作 为必填项<br/>");
                    }
                    if (StringUtils.isAllBlank(dto.getSrcIp(), dto.getDstIp())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，源地址、目的地址不能都为空，必填一项<br/>");
                    }
                } else if (type == 2) {
                    if (!StringUtils.isNoneBlank(dto.getStrIpv6(), dto.getAction())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，IP地址类型、动作 为必填项<br/>");
                    }
                    if (StringUtils.isAllBlank(dto.getSrcIp(), dto.getDstIp())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，源地址、目的地址不能都为空，必填一项<br/>");
                    }
                    if (StringUtils.isNoneBlank(dto.getSrcIp(), dto.getDstIp())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，源地址、目的地址只能填写一项<br/>");
                    }
                }
            } else if (category == 1) {
                /*开始验证>>路由封堵>>*/
                if (type == 1) {
                    if (!StringUtils.isNoneBlank(dto.getStrIpv6(), dto.getRoutingIp(), dto.getAction())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，IP地址类型、黑洞路由IP、动作 为必填项<br/>");
                    }
                } else if (type == 2) {
                    if (!StringUtils.isNoneBlank(dto.getStrIpv6(), dto.getRoutingIp(), dto.getAction())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，IP地址类型、黑洞路由IP、动作 为必填项<br/>");
                    }
                }
            }

            //开始验证五元组
            //源地址
            if (StringUtils.isNoneBlank(dto.getSrcIp(), dto.getStrIpv6())) {
                ResultRO<String> srcIpResult = ImportExcelVerUtils.checkIpByIpv46(dto.getSrcIp(), "源地址", dto.getStrIpv6());
                if (srcIpResult.getSuccess()) {
                    dto.setSrcIp(srcIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(srcIpResult.getMessage())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，" + srcIpResult.getMessage() + " <br/>");
                    }
                }
            }

            //目的地址
            if (StringUtils.isNoneBlank(dto.getDstIp(), dto.getStrIpv6())) {
                ResultRO<String> dstIpResult = ImportExcelVerUtils.checkIpByIpv46(dto.getDstIp(), "目的地址", dto.getStrIpv6());
                if (dstIpResult.getSuccess()) {
                    dto.setDstIp(dstIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(dstIpResult.getMessage())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，" + dstIpResult.getMessage() + " <br/>");
                    }
                }
            }

            //服务
            if (StringUtils.isNoneBlank(dto.getServiceList())) {
                ResultRO<List<ServiceDTO>> serviceResult = ImportExcelVerUtils.getServiceList(dto.getServiceList(),dto.getStrIpv6());
                if (serviceResult.getSuccess()) {
                    dto.setServiceList(ServiceDTOUtils.toString(serviceResult.getData()));
                } else {
                    if (StringUtils.isNoneBlank(serviceResult.getMessage())) {
                        List<String> error = JSONObject.parseArray(serviceResult.getMessage(), String.class);
                        if (error != null && !error.isEmpty()) {
                            for (String msg : error) {
                                failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，" + msg + " <br/>");
                            }
                        }
                    }
                }
            }

            //黑洞路由IP
            if (StringUtils.isNoneBlank(dto.getRoutingIp(), dto.getStrIpv6())) {
                ResultRO<String> routingIpResult = ImportExcelVerUtils.checkIpByIpv46(dto.getRoutingIp(), "黑洞路由IP", dto.getStrIpv6());
                if (routingIpResult.getSuccess()) {
                    dto.setRoutingIp(routingIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(routingIpResult.getMessage())) {
                        failureMsg.append(logMsgPrefix + "第" + rowIndex + "行，" + routingIpResult.getMessage() + " <br/>");
                    }
                }
            }

            rowIndex++;
        }

        //只要有异常信息，就返回，不做入库操作
        if (StringUtils.isNotBlank(failureMsg)) {
            return null;
        }
        //数据过滤
        List<DisposalOrderDTO> dataList = new ArrayList<>();
        for (ImportOrderDTO dto : batchList) {
            //过滤非法数据 （不能为空的数据）
            if (category == 0) {
                if (type == 1) {
                    if (!StringUtils.isNoneBlank(dto.getOrderName(), dto.getScenesNames(), dto.getStrIpv6(), dto.getAction())) {
                        continue;
                    }
                    if (StringUtils.isAllBlank(dto.getSrcIp(), dto.getDstIp())) {
                        continue;
                    }
                } else if (type == 2) {
                    if (!StringUtils.isNoneBlank(dto.getOrderName(), dto.getStrIpv6(), dto.getAction())) {
                        continue;
                    }
                    if (StringUtils.isAllBlank(dto.getSrcIp(), dto.getDstIp())) {
                        continue;
                    }
                    if (StringUtils.isNoneBlank(dto.getSrcIp(), dto.getDstIp())) {
                        continue;
                    }
                }
            } else if (category == 1) {
                if (type == 1) {
                    if (!StringUtils.isNoneBlank(dto.getOrderName(), dto.getScenesNames(), dto.getStrIpv6(), dto.getRoutingIp(), dto.getAction())) {
                        continue;
                    }
                } else if (type == 2) {
                    if (!StringUtils.isNoneBlank(dto.getOrderName(), dto.getStrIpv6(), dto.getRoutingIp(), dto.getAction())) {
                        continue;
                    }
                }
            }

            DisposalOrderDTO orderDTO = new DisposalOrderDTO();
            dozerMapper.map(dto, orderDTO);
            orderDTO.setStatus(0);
            orderDTO.setCreateUser(userName);
            orderDTO.setCategory(category);
            orderDTO.setType(type);

            if (orderDTO.getAction().equals("封堵")) {
                orderDTO.setAction("deny");
            } else if (orderDTO.getAction().equals("解封")) {
                orderDTO.setAction("permit");
            } else {
                orderDTO.setAction("deny");
            }

            List<String> scenesUuidList = new ArrayList<>();
            //场景名称转换场景UUID
            if (StringUtils.isNoneBlank(dto.getScenesNames())) {
                if (dto.getScenesNames().contains(",")) {
                    String[] scenesNameArray = dto.getScenesNames().split(",");
                    for (String scenesName : scenesNameArray) {
                        if (scenesMap.get(scenesName) != null) {
                            scenesUuidList.add(scenesMap.get(scenesName));
                        }
                    }
                } else {
                    if (scenesMap.get(dto.getScenesNames()) != null) {
                        scenesUuidList.add(scenesMap.get(dto.getScenesNames()));
                    }
                }
            }
            if (scenesUuidList.size() > 0) {
                orderDTO.setScenesUuidArray(scenesUuidList.toArray(new String[0]));
            }

            //策略封堵>>黑IP自动封堵>>源地址和目的地址只能填写一项，故单独处理
            if (orderDTO.getCategory() == 0 && orderDTO.getType() == 2) {
                if (StringUtils.isBlank(orderDTO.getSrcIp()) && StringUtils.isNotBlank(orderDTO.getDstIp())) {
                    orderDTO.setSrcIp(null);
                }
                if (StringUtils.isBlank(orderDTO.getDstIp()) && StringUtils.isNotBlank(orderDTO.getSrcIp())) {
                    orderDTO.setDstIp(null);
                }
            } else {
                // 源ip 目的ip 判断ipv6 ipv4 赋值any
                if (AliStringUtils.isEmpty(orderDTO.getSrcIp())) {
                    if (dto.getStrIpv6() != null && dto.getStrIpv6().equals("IPV6")) {
                        orderDTO.setSrcIp(PolicyConstants.IPV6_ANY);
                    } else {
                        orderDTO.setSrcIp(PolicyConstants.IPV4_ANY);
                    }
                }
                if (AliStringUtils.isEmpty(orderDTO.getDstIp())) {
                    if (dto.getStrIpv6() != null && dto.getStrIpv6().equals("IPV6")) {
                        orderDTO.setDstIp(PolicyConstants.IPV6_ANY);
                    } else {
                        orderDTO.setDstIp(PolicyConstants.IPV4_ANY);
                    }
                }
            }

            dataList.add(orderDTO);
        }

        if(dataList == null ||  dataList.isEmpty()){
            return Collections.emptyList();
        }

        return dataList;
    }



    @KafkaListener(topics = CommonConfigParam.STR_TOPIC_HANDLE_CALLBACK)
    public void callback(String jsonStr) {
        logger.info("接收派发下级单位协作返回数据:"+jsonStr);
        DisposalHandleCallbackDTO handleCallbackDTO = JSONObject.parseObject(jsonStr, DisposalHandleCallbackDTO.class);
        if (handleCallbackDTO == null) {
            logger.error(CommonConfigParam.STR_TOPIC_HANDLE_CALLBACK, "kafka 协作处置状态回调 topic 数据为null！");
        } else {
            if (AliStringUtils.areNotEmpty(handleCallbackDTO.getCenterUuid(), handleCallbackDTO.getWorkIp()
                    , handleCallbackDTO.getWorkName()) && handleCallbackDTO.getCallbackFlag() != null) {

                logger.info(CommonConfigParam.STR_TOPIC_HANDLE_CALLBACK, JSONObject.toJSONString(handleCallbackDTO));

                DisposalTeamBranchEntity teamBranchEntity = new DisposalTeamBranchEntity();
                teamBranchEntity.setCenterUuid(handleCallbackDTO.getCenterUuid());
                teamBranchEntity.setBranchIp(handleCallbackDTO.getWorkIp());
                teamBranchEntity.setBranchName(handleCallbackDTO.getWorkName());
                if (handleCallbackDTO.getCallbackFlag()) {
                    if (handleCallbackDTO.getSuccess()) {
                        teamBranchEntity.setCallbackHandleStatus(1);
                    } else {
                        teamBranchEntity.setCallbackHandleStatus(2);
                    }
                    //更新 派发下级单位回滚处置单状态
                    ReturnT<String> returnT = disposalTeamBranchService.updateCallbackHandleStatus(teamBranchEntity);
                    logger.info(returnT.toString());
                } else {
                    if (handleCallbackDTO.getSuccess()) {
                        teamBranchEntity.setHandleStatus(1);
                    } else {
                        teamBranchEntity.setHandleStatus(2);
                    }
                    //更新 派发下级单位处置单状态
                    ReturnT<String> returnT = disposalTeamBranchService.updateHandleStatus(teamBranchEntity);
                    logger.info(returnT.toString());
                }

            } else {
                logger.error(CommonConfigParam.STR_TOPIC_HANDLE_CALLBACK, "kafka 协作处置状态回调 topic 数据缺失！");
            }
        }
    }

}

