package com.abtnetworks.totems.credential.controller;

import com.abtnetworks.data.totems.log.client.LogClientSimple;
import com.abtnetworks.data.totems.log.common.enums.BusinessLogType;
import com.abtnetworks.data.totems.log.common.enums.LogLevel;
import com.abtnetworks.totems.common.BaseController;
import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.NodeTypeEnum;
import com.abtnetworks.totems.common.executor.PolicyAddThread;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.signature.ReturnT;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.Encodes;
import com.abtnetworks.totems.common.utils.FileUtils;
import com.abtnetworks.totems.common.utils.excel.TotemsExcelExport;
import com.abtnetworks.totems.credential.dao.mysql.CredentialMapper;
import com.abtnetworks.totems.credential.dto.CredentialDTO;
import com.abtnetworks.totems.credential.dto.SearchCredentialByPageDTO;
import com.abtnetworks.totems.credential.dto.UpdateCredentialDTO;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.credential.entity.ExcelCredentialEntity;
import com.abtnetworks.totems.credential.service.CredentialService;
import com.abtnetworks.totems.credential.vo.CredentialVO;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.utils.ExportExcelUtils;
import com.abtnetworks.totems.whale.baseapi.ro.CredentialResultRO;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/8 12:57
 */
@Api(value="????????????")
@RestController
@RequestMapping(value = "/credential/")
public class CredentialController extends BaseController {

    @Autowired
    private CredentialService service;

    @Value("${resourceHandler}")
    private String resourceHandler;

    @Value("${importCredentialExcelFile}")
    private String credentialExcelFile;

    @Value("${push.download-file}")
    String dirPath;

    @Autowired
    ExcelParser generateExcelParser;

    @Autowired
    private LogClientSimple logClientSimple;

    @ApiOperation("????????????????????????")
    @PostMapping("getall")
    public ReturnResult getAll(@RequestBody SearchCredentialByPageDTO searchCredentialByPageDTO, Authentication authentication) {
        logger.debug("????????????????????????START");
        String userName = authentication.getName();
        ReturnResult returnResult = new ReturnResult();
        try {
            PageInfo<CredentialVO> pageInfo = service.getAll(searchCredentialByPageDTO,userName);
            returnResult.setContent(pageInfo);
            returnResult.setCode(ReturnResult.SUCCESS_CODE);
            logger.debug("????????????????????????END");
            return returnResult;
        }catch (Exception e){
            logger.error("????????????????????????????????????END", e);
            returnResult.setCode(ReturnResult.FAIL_CODE);
            returnResult.setMsg("????????????");
        }

        return returnResult;
    }

    @ApiOperation("????????????????????????")
    @ApiImplicitParam(paramType="query", name = "uuid", value = "UUID", required = true, dataType = "String")
    @PostMapping("get")
    public CredentialResultRO get(String uuid) {
        logger.info("get(" + uuid + ")");
        CredentialResultRO result = service.get(uuid);

        return result;
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType="query", name = "name", value = "?????????", required = true, dataType = "String"),
        @ApiImplicitParam(paramType="query", name = "loginName", value = "?????????", required = true, dataType = "String"),
        @ApiImplicitParam(paramType="query", name = "loginPassword", value = "??????", required = true, dataType = "String"),
        @ApiImplicitParam(paramType="query", name = "enableUserName", value = "???????????????", required = false, dataType = "String"),
        @ApiImplicitParam(paramType="query", name = "enablePassword", value = "????????????", required = false, dataType = "String"),
    })
    @PostMapping("create")
    public ReturnResult create(@RequestBody CredentialDTO dto, Authentication authentication) {
        if(dto.getDescription() == null) {
            dto.setDescription("");
        }
        String userName;
        if(StringUtils.isNotEmpty(dto.getUserName())){
            userName  = dto.getUserName();
        }else {
            userName  = authentication.getName();
        }
        try {
            int rc = service.create(dto.getName(), dto.getDescription(), dto.getLoginName(), dto.getLoginPassword(), dto.getEnableUserName(), dto.getEnablePassword(), userName, true);
            if (ReturnCode.CREDENTIAL_VALIDATION_DUPLICATE_NAME == rc) {
                return new ReturnResult(ReturnResult.FAIL_CODE, "??????????????????????????????");
            }
            if (ReturnCode.FAILED_TO_CREATE_CREDENTIAL == rc) {
                return new ReturnResult(ReturnResult.FAIL_CODE, "????????????");
            }
        }catch (Exception e){
            return new ReturnResult(ReturnResult.FAIL_CODE,"?????????????????????????????????????????????");
        }

        return ReturnResult.SUCCESS;
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="query", name = "id", value = "id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name = "uuid", value = "uuid", required = true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name = "name", value = "?????????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name = "loginName", value = "?????????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name = "loginPassword", value = "??????", required = true, dataType = "String"),
            @ApiImplicitParam(paramType="query", name = "enableUserName", value = "???????????????", required = false, dataType = "String"),
            @ApiImplicitParam(paramType="query", name = "enablePassword", value = "????????????", required = false, dataType = "String"),
    })
    @PostMapping("modify")
    public ReturnResult modify(@RequestBody CredentialDTO dto,Authentication authentication) {
        if(dto.getDescription() == null) {
            dto.setDescription("");
        }
        String userName;
        if(StringUtils.isNotEmpty(dto.getUserName())){
            userName  = dto.getUserName();
        }else {
            userName  = authentication.getName();
        }
        try{
        int rc = service.modify(dto.getId(), dto.getUuid(), dto.getName(), dto.getDescription(), dto.getLoginName(), dto.getLoginPassword(), dto.getEnableUserName(), dto.getEnablePassword(), dto.getVersion(),userName, true);
            if(ReturnCode.FAILED_TO_CREATE_CREDENTIAL == rc){
                return new ReturnResult(ReturnResult.FAIL_CODE,"????????????");
            }
        }catch (IllegalArgumentException e){
            return new ReturnResult(ReturnResult.FAIL_CODE,e.getMessage());
        }catch (Exception e){
            return new ReturnResult(ReturnResult.FAIL_CODE,"?????????????????????????????????????????????");
        }
        return ReturnResult.SUCCESS;
    }

    @ApiOperation("??????????????????")
    @ApiImplicitParam(paramType="query", name = "uuid", value = "UUID", required = true, dataType = "String")
    @PostMapping("delete")
    public JSONObject delete(String uuid) {
        int rc = 0;
        try{
            rc = service.delete(uuid);
        } catch (Exception e){
            logger.error("??????????????????????????????????????????", e);
        }

        return getReturnJSON(rc);
    }

    @ApiOperation("??????????????????????????????")
    @PostMapping("download-credential-template")
    public JSONObject downloadCredentialTemplate() {
        String status = "-1";
        String errCode = "";
        String errMsg = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", resourceHandler.replace("**", "") + credentialExcelFile);
            status = "0";
        } catch (Exception e) {
            errMsg += e;
            logger.error("downloadCredentialTemplate error???" + e);
        }

        return returnJSON(status, jsonObject, errCode, errMsg);
    }

    @ApiOperation(value = "??????????????????", httpMethod = "POST", notes = "????????????Excel????????????????????????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "encrypt", value = "????????????", required = true, dataType = "Boolean")
    })
    @PostMapping("import-credential")
    public ReturnT importCredential(MultipartFile file, Authentication auth, HttpServletResponse response, Boolean encrypt) {
        logger.info("????????????????????????");
        String msg;
        List<ExcelCredentialEntity> tmpList = new ArrayList<>();
        msg = generateExcelParser.parseCredentialExcel(file, auth.getName(), tmpList, encrypt);
        if(StringUtils.isNotEmpty(msg) ) {
            return new ReturnT(ReturnT.FAIL_CODE, msg);
        }

        msg = String.format("???????????????????????????????????????%s???", tmpList.size());
        logClientSimple.addBusinessLog(LogLevel.INFO.getId(), BusinessLogType.SYNTHESIZE_CONFIGURE.getId(), msg);
        return new ReturnT(ReturnT.SUCCESS_CODE, msg);
    }
    @PostMapping("batch-credential-update")
    public ReturnT batchUpdateCredential(@RequestBody UpdateCredentialDTO updateCredentialDTO,Authentication authentication){
        logger.debug("?????????????????????start");
        try{
            service.batchModify(updateCredentialDTO,authentication);
            return ReturnT.SUCCESS;
        }catch (IllegalAccessException e){
            logger.error("????????????????????????",e);
            return new ReturnT(ReturnT.FAIL_CODE, e.getMessage());
        }catch (Exception e){
            logger.error("????????????",e);
            return new ReturnT(ReturnT.FAIL_CODE, "??????????????????");
        }

    }

    @ApiOperation(value = "??????excel??????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "??????????????????", required = false, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "")})
    @RequestMapping(value = "export-credential", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload, Authentication authentication) throws Exception {
        List<CredentialEntity> credentialList = service.getCredentialList(authentication.getName());

        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "????????????";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            logger.error("????????????????????????????????????", e1);
        }

        String destDirName = dirPath + "/credentailExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "credentaildoing.temp";

        try {
            // ???????????????????????????
            if (!new File(destDirName).exists()) {
                FileUtils.createDir(destDirName);
            }

            String fileIsExistsName = FileUtils.isDirExistFile(destDirName);
            boolean doingFileTempIsExists = FileUtils.fileIsExists(doingFileTemp);
            boolean fileIsExists = FileUtils.fileIsExists(destDirName + "/" + fileIsExistsName);
            if (null == isReload) {
                if (fileIsExists && doingFileTempIsExists == false) {
                    resultRO.setMessage("??????????????????");
                    jsonObject.put("filePath", fileIsExistsName);
                    jsonObject.put("status", 1);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists) {
                    // ??????????????????????????????
                    resultRO.setMessage("???????????????");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists == false && fileIsExists == false) {
                    // ??????????????????
                    File doingFile = new File(doingFileTemp);
                    doingFile.createNewFile();
                    resultRO.setMessage("????????????");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    new CerdentailExportThread(filePath, doingFile, credentialList).start();
                    resultRO.setData(jsonObject);
                    return resultRO;
                }
            }
            if ("true".equals(isReload)) {
                // ??????????????????
                FileUtils.deleteFileByPath(destDirName + "/" + fileIsExistsName);
                // ?????????????????????????????????
                // ??????????????????
                File doingFile = new File(doingFileTemp);
                doingFile.createNewFile();
                new CerdentailExportThread(filePath, doingFile, credentialList).start();
                resultRO.setMessage("??????????????????");
                jsonObject.put("filePath", preFilename + ".xlsx");
                jsonObject.put("status", 2);
                resultRO.setData(jsonObject);
                return resultRO;
            } else {
                downLoadCredential(response, destDirName + "/" + fileIsExistsName);
                return null;
            }
        } catch (Exception e) {
            File doingFile = new File(doingFileTemp);
            doingFile.delete();
            logger.error("??????????????????Excel????????????:", e);
            resultRO.setMessage("??????????????????");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }

    /**
     * ?????????????????????????????????,????????????excel
     */
    private class CerdentailExportThread extends Thread {
        private String filePath;
        private File doingFile;
        private List<CredentialEntity> credentialList;

        public CerdentailExportThread(String filePath, File doingFile, List<CredentialEntity> credentialList) {
            super();
            this.filePath = filePath;
            this.doingFile = doingFile;
            this.credentialList = credentialList;
        }

        public CerdentailExportThread() {
            super();
        }

        @Override
        public void run() {
            OutputStream out = null;
            try {
                out = new FileOutputStream(filePath);

                String[] credentialHeaders = { "?????????", "?????????",  "??????", "???????????????", "????????????"};
                List<List<String>> credentialData = new ArrayList<List<String>>();
                for(CredentialEntity credential : credentialList){
                    List<String> rowData = new ArrayList<>();
                    rowData.add(credential.getName());
                    rowData.add(credential.getLoginName());
                    rowData.add(credential.getLoginPassword());
                    rowData.add(credential.getEnableUserName());
                    rowData.add(credential.getEnablePassword());
                    credentialData.add(rowData);
                }
                ExportExcelUtils eeu = new ExportExcelUtils();
                XSSFWorkbook workbook = new XSSFWorkbook();
                eeu.exportCommonData(workbook, 0, "????????????", credentialHeaders, credentialData, out);

                // ????????????????????????????????????????????????????????????????????????
                workbook.write(out);
            } catch (Exception e) {
                File file = new File(filePath);
                if(file.exists()) {
                    file.delete();
                }
                logger.error("??????????????????excel??????", e);
            }finally {
                try{
                    if(out != null) {
                        out.close();
                    }
                }catch (IOException e1){
                    logger.error("???????????????");
                }
            }
            doingFile.delete();
        }
    }

    /**
     * ????????????
     * @param response
     * @param fileExcitPath
     */
    public void downLoadCredential(HttpServletResponse response, String fileExcitPath) {
        File src = new File(fileExcitPath);
        FileUtils.downloadOverView(src, response);
    }

}
