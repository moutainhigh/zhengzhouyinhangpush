package com.abtnetworks.totems.disposal.controller;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.tools.excel.ImportExcel;
import com.abtnetworks.totems.common.utils.IPUtil;
import com.abtnetworks.totems.common.utils.ImportExcelVerUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.disposal.ReturnT;
import com.abtnetworks.totems.disposal.dto.DisposalWhiteSaveDTO;
import com.abtnetworks.totems.disposal.dto.ImportPolicyWhiteListDTO;
import com.abtnetworks.totems.disposal.dto.ImportRoutWhiteListDTO;
import com.abtnetworks.totems.disposal.entity.DisposalWhiteListEntity;
import com.abtnetworks.totems.disposal.enums.DisposalCategoryEnum;
import com.abtnetworks.totems.disposal.enums.DisposalTypeEnum;
import com.abtnetworks.totems.disposal.service.DisposalWhiteListService;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.abtnetworks.totems.disposal.BaseController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @Author hw
 * @Description
 * @Date 16:20 2019/11/11
 */
@Api(tags = {"白名单管理"})
@RestController
@RequestMapping(value = "${startPath}/disposal/whiteList")
public class DisposalWhiteListController extends BaseController {

    @Autowired
    private DisposalWhiteListService disposalWhiteListService;

    @Value("${resourceHandler}")
    private String resourceHandler;

    @Value("${importWhiteListFileName}")
    private String importWhiteListFileName;

    @ApiOperation(value = "新增", nickname = "鲁薇")
    @PostMapping("/insert")
    public ResultRO insert(Authentication authentication,
                                  @ApiParam(name = "vo", value = "白名单对象json格式", required = true) @RequestBody DisposalWhiteSaveDTO vo){
        if(authentication != null){
            vo.setCreateUser(authentication.getName());
        }
        return disposalWhiteListService.insert(vo);
    }

    /**
     * 删除
     */
    @ApiOperation(value = "删除", nickname = "鲁薇")
    @PostMapping("/delete")
    public ResultRO delete(Authentication authentication, @ApiParam(name = "ids", value = "流水记录id，多个用逗号隔开", required = true) @RequestParam String ids){
        String modifiedUser = "";
        if(authentication != null){
            modifiedUser =  authentication.getName();
        }
        return disposalWhiteListService.delete(modifiedUser, ids);
    }

    @ApiOperation(value = "更新", nickname = "鲁薇")
    @PostMapping("/update")
    public ResultRO update(Authentication authentication,
                                  @ApiParam(name = "vo", value = "白名单对象json格式", required = true) @RequestBody  DisposalWhiteSaveDTO vo){
        if(authentication != null){
            vo.setModifiedUser(authentication.getName());
        }
        return disposalWhiteListService.update(vo);
    }

    @ApiOperation(value = "根据id查询", nickname = "鲁薇")
    @PostMapping("/getById")
    public ResultRO<DisposalWhiteListEntity> getById(Long id){
        return disposalWhiteListService.getById(id);
    }


    @ApiOperation(value = "分页查询", nickname = "鲁薇")
    @PostMapping("/pageList")
    public ResultRO<List<DisposalWhiteListEntity>> pageList(@ApiParam(name = "type", value = "类型：0策略、1路由", required = false, defaultValue = "0") @RequestParam(required = false)  Integer type,
                                                            @ApiParam(name = "name", value = "白名单名称", required = false, defaultValue = "123") @RequestParam(required = false)  String name,
                                                            @ApiParam(name = "content", value = "内容", required = false, defaultValue = "192.168") @RequestParam(required = false)  String content,
                                                            @ApiParam(name = "currentPage", value = "当前页", required = true, defaultValue = "1") @RequestParam(required = true)  Integer currentPage,
                                                            @ApiParam(name = "pageSize", value = "每页显示的记录条数", required = true, defaultValue = "20") @RequestParam(required = true)  Integer pageSize) {
        return disposalWhiteListService.findList(type,name, content, currentPage, pageSize);
    }


    @ApiOperation(value = "下载导入白名单Excel模板", httpMethod = "POST", nickname = "鲁薇")
    @RequestMapping(value = "/download-import-template",  method = RequestMethod.POST)
    public ResultRO<String> downloadImportTemplate() {
        ResultRO<String> resultRO = new ResultRO<>(true);
        resultRO.setData(resourceHandler.replace("**", "") + importWhiteListFileName);
        return resultRO;
    }

    @ApiOperation(value = "导入白名单", httpMethod = "POST", nickname = "鲁薇")
    @RequestMapping(value = "/batch-import",  method = RequestMethod.POST)
    public ResultRO batchImport(MultipartFile file, Authentication auth){
        logger.info("----------------------------导入白名单开始");
        String errorMsg = "";

        try{
            if(file == null){
                return new ResultRO(false, "文件不能为空");
            }

            String userName = "";
            if(auth != null){
                userName = auth.getName();
            }

            //校验后缀格式
            String fileName = file.getOriginalFilename();
            logger.info("导入上传的文件名称: " + fileName);
            String suffix = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
            logger.info("导入的文件后缀,suffix:{}", suffix);
            if (!suffix.equals(".xls") && !suffix.equals(".xlsx")) {
                return new ResultRO(false, "文件格式错误，请使用标准模板上传");
            }

            StringBuilder failureMsg = new StringBuilder();
            //路由白名单导入
            ImportExcel policyEi = new ImportExcel(file, 0, 0);
            //路由白名单导入
            ImportExcel routEi = new ImportExcel(file, 0, 1);

            //读取sheet数据
            List<ImportPolicyWhiteListDTO> policyList = policyEi.getDataList(ImportPolicyWhiteListDTO.class);
            List<ImportRoutWhiteListDTO> routList = routEi.getDataList(ImportRoutWhiteListDTO.class);

            if ((policyList == null || policyList.isEmpty()) && (routList == null || routList.isEmpty())) {
                return new ResultRO(false, "文件无数据");
            }

            //获取库中已存在的白名单
            Map<String,String> nameMap = getAllWhiteName();
            //当前excel中的白名单
            Map<String, String> currentNameMap = new HashMap<>();

            //校验
            List<DisposalWhiteSaveDTO> policyDataList = checkPolicyWhite(policyList, nameMap, currentNameMap, failureMsg, userName);
            List<DisposalWhiteSaveDTO> routDataList = checkRoutWhite(routList, nameMap, currentNameMap, failureMsg, userName);
            errorMsg = failureMsg.toString();

            //只要有异常信息，就返回，不做入库操作
            if (StringUtils.isNotBlank(errorMsg)) {
                return new ResultRO(false, errorMsg);
            }

            //入库
            List<DisposalWhiteSaveDTO> waitSaveList = new ArrayList<>();
            waitSaveList.addAll(policyDataList);
            waitSaveList.addAll(routDataList);
            ResultRO resultRO = disposalWhiteListService.batchInsert(waitSaveList);

            if(!resultRO.getSuccess()){
                return resultRO;
            }

        }catch (RuntimeException e) {
            errorMsg = "导入失败，请确保采集凭据与品牌、型号能正确关联!";
            logger.error("导入任务列表失败：", e);
        } catch (Exception e) {
            errorMsg = "文件格式错误，请使用标准模板上传!";
            logger.error("导入任务列表失败：", e);
        }
        logger.info("----------------------------导入白名单结束");

        if (StringUtils.isNotBlank(errorMsg)) {
            return new ResultRO(false, errorMsg);
        }

        return new ResultRO<>(true, "导入成功");
    }


    private List<DisposalWhiteSaveDTO> checkPolicyWhite(List<ImportPolicyWhiteListDTO> batchList,
                                                            Map<String, String> nameMap, Map<String, String> currentNameMap,
                                                            StringBuilder failureMsg, String userName){
        if(batchList == null ||  batchList.isEmpty()){
            return Collections.emptyList();
        }

        int beforLength = failureMsg.length();

        int rowIndex = 2;
        for (ImportPolicyWhiteListDTO dto : batchList) {
            //过滤空行
            if (StringUtils.isAllBlank(dto.getName(), dto.getSrcIp(), dto.getDstIp(), dto.getService(), dto.getRemarks())) {
                rowIndex++;
                continue;
            }

            if (StringUtils.isBlank(dto.getName())) {
                failureMsg.append("第" + rowIndex + "行，名称不能为空<br/>");
            } else if (StringUtils.isNoneBlank(dto.getName())) {
                if (currentNameMap.get(dto.getName()) != null) {
                    failureMsg.append("第" + rowIndex + "行，名称" + dto.getName() + "在模板中存在多条<br/>");
                } else if (nameMap.get(dto.getName()) != null) {
                    failureMsg.append("第" + rowIndex + "行，名称" + dto.getName() + "在系统中已存在<br/>");
                }else{
                    currentNameMap.put(dto.getName(), dto.getName());
                }
            }

            if(StringUtils.isBlank(dto.getIpType())){
                failureMsg.append("第" + rowIndex + "行，IP类型不能为空<br/>");
            }


            //源地址
            if (StringUtils.isNoneBlank(dto.getSrcIp())) {
                ResultRO<String> srcIpResult = ImportExcelVerUtils.checkIpByIpv46(dto.getSrcIp(), "源地址", dto.getIpType());
                if (srcIpResult.getSuccess()) {
                    dto.setSrcIpText(srcIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(srcIpResult.getMessage())) {
                        failureMsg.append("第" + rowIndex + "行，" + srcIpResult.getMessage() + " <br/>");
                    }
                }
            }

            //目的地址
            if (StringUtils.isNoneBlank(dto.getDstIp())) {
                ResultRO<String> dstIpResult = ImportExcelVerUtils.checkIpByIpv46(dto.getDstIp(), "目的地址", dto.getIpType());
                if (dstIpResult.getSuccess()) {
                    dto.setDstIpText(dstIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(dstIpResult.getMessage())) {
                        failureMsg.append("第" + rowIndex + "行，" + dstIpResult.getMessage() + " <br/>");
                    }
                }
            }

            //服务
            if (StringUtils.isNoneBlank(dto.getService())) {
                ResultRO<List<ServiceDTO>> serviceResult = ImportExcelVerUtils.getServiceList(dto.getService(),dto.getIpType());
                if (serviceResult.getSuccess()) {
                    dto.setServiceList(serviceResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(serviceResult.getMessage())) {
                        List<String> error = JSONObject.parseArray(serviceResult.getMessage(), String.class);
                        if (error != null && !error.isEmpty()) {
                            for (String msg : error) {
                                failureMsg.append("第" + rowIndex + "行，" + msg + " <br/>");
                            }
                        }
                    }
                }
            }

            rowIndex++;
        }

        //数据过滤
        List<DisposalWhiteSaveDTO> dataList = new ArrayList<>();
        for (ImportPolicyWhiteListDTO dto : batchList) {
            if (StringUtils.isAllBlank(dto.getName(), dto.getSrcIp(), dto.getDstIp(), dto.getService(), dto.getRemarks())) {
                continue;
            }
            DisposalWhiteSaveDTO saveDTO = new DisposalWhiteSaveDTO();
            saveDTO.setType(DisposalCategoryEnum.POLICY.getCode());
            saveDTO.setCreateUser(userName);
            saveDTO.setName(dto.getName());
            saveDTO.setSrcIp(dto.getSrcIpText());
            saveDTO.setDstIp(dto.getDstIpText());
            saveDTO.setRemarks(dto.getRemarks());
            if (dto.getIpType() != null && ("是".equals(dto.getIpType()) || "IPV6".equals(dto.getIpType()))) {
                saveDTO.setIpv6(true);
            } else {
                saveDTO.setIpv6(false);
            }
            //服务进行格式转换
            if (dto.getServiceList() != null && !dto.getServiceList().isEmpty()) {
                saveDTO.setServiceList(dto.getServiceList());
            }
            dataList.add(saveDTO);
        }

        if(dataList == null ||  dataList.isEmpty()){
            return Collections.emptyList();
        }

        int afterLength = failureMsg.length();
        if(afterLength > beforLength){
            failureMsg.insert(beforLength, "策略白名单错误详情：<br/>");
        }

        return dataList;
    }


    private List<DisposalWhiteSaveDTO> checkRoutWhite(List<ImportRoutWhiteListDTO> batchList,
                                                      Map<String, String> nameMap, Map<String, String> currentNameMap,
                                                      StringBuilder failureMsg, String userName){
        if(batchList == null ||  batchList.isEmpty()){
            return Collections.emptyList();
        }

        int beforLength = failureMsg.length();

        int rowIndex = 2;
        for (ImportRoutWhiteListDTO dto : batchList) {
            //过滤空行
            if (StringUtils.isAllBlank(dto.getName(), dto.getRoutingIp(), dto.getRemarks())) {
                rowIndex++;
                continue;
            }

            if (StringUtils.isBlank(dto.getName())) {
                failureMsg.append("第" + rowIndex + "行，名称不能为空<br/>");
            } else if (StringUtils.isNoneBlank(dto.getName())) {
                if (currentNameMap.get(dto.getName()) != null) {
                    failureMsg.append("第" + rowIndex + "行，名称" + dto.getName() + "在模板中存在多条<br/>");
                } else if (nameMap.get(dto.getName()) != null) {
                    failureMsg.append("第" + rowIndex + "行，名称" + dto.getName() + "在系统中已存在<br/>");
                } else {
                    currentNameMap.put(dto.getName(), dto.getName());
                }
            }

            if (StringUtils.isBlank(dto.getRoutingIp())) {
                failureMsg.append("第" + rowIndex + "行，黑洞路由IP不能为空<br/>");
            }else{
                ResultRO<String> srcIpResult = ImportExcelVerUtils.checkIpByIpv46(dto.getRoutingIp(), "黑洞路由IP", ImportExcelVerUtils.IP_TYPE_IPV46);
                if (srcIpResult.getSuccess()) {
                    dto.setRoutingIpText(srcIpResult.getData());
                } else {
                    if (StringUtils.isNoneBlank(srcIpResult.getMessage())) {
                        failureMsg.append("第" + rowIndex + "行，" + srcIpResult.getMessage() + " <br/>");
                    }
                }
            }

            rowIndex++;
        }

        //数据过滤
        List<DisposalWhiteSaveDTO> dataList = new ArrayList<>();
        for (ImportRoutWhiteListDTO dto : batchList) {
            if (StringUtils.isAllBlank(dto.getName(),  dto.getRoutingIp(), dto.getRemarks())) {
                continue;
            }
            DisposalWhiteSaveDTO saveDTO = new DisposalWhiteSaveDTO();
            saveDTO.setType(DisposalCategoryEnum.ROUT.getCode());
            saveDTO.setCreateUser(userName);
            saveDTO.setName(dto.getName());
            saveDTO.setRoutingIp(dto.getRoutingIpText());
            saveDTO.setRemarks(dto.getRemarks());
            dataList.add(saveDTO);
        }

        if(dataList == null ||  dataList.isEmpty()){
            return Collections.emptyList();
        }

        int afterLength = failureMsg.length();
        if(afterLength > beforLength){
            failureMsg.insert(beforLength, "路由白名单错误详情：<br/>");
        }

        return dataList;
    }

    //获取DB中的 白名单名称
    private Map<String, String> getAllWhiteName(){
        Map<String, String> existsMap = new HashMap<>();
        ResultRO<List<DisposalWhiteListEntity>> resultRO = disposalWhiteListService.findAll();
        if(resultRO == null || !resultRO.getSuccess() || resultRO.getData() == null || resultRO.getData().isEmpty()){
            return existsMap;
        }

        for(DisposalWhiteListEntity po : resultRO.getData()){
            existsMap.put(po.getName(), po.getName());
        }

        return existsMap;
    }
}

