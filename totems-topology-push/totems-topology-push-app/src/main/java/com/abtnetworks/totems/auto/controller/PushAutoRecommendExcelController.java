package com.abtnetworks.totems.auto.controller;

import com.abtnetworks.totems.auto.entity.AutoRecommendTaskEntity;
import com.abtnetworks.totems.auto.entity.ProtectNetworkConfigEntity;
import com.abtnetworks.totems.auto.entity.ProtectNetworkNatMappingEntity;
import com.abtnetworks.totems.auto.enums.PushNatFlagEnum;
import com.abtnetworks.totems.auto.enums.PushNatTypeEnum;
import com.abtnetworks.totems.auto.service.PushAutoRecommendExcelService;
import com.abtnetworks.totems.auto.service.PushAutoRecommendService;
import com.abtnetworks.totems.auto.service.PushProtectNetworkConfigService;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskSearchVO;
import com.abtnetworks.totems.auto.vo.AutoRecommendTaskVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigSearchVO;
import com.abtnetworks.totems.auto.vo.ProtectNetworkConfigVO;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedExecutor;
import com.abtnetworks.totems.common.executor.ExtendedLatchRunnable;
import com.abtnetworks.totems.common.executor.PushAutoGenerateThread;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.tools.excel.ExcelParser;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.DateUtil;
import com.abtnetworks.totems.common.utils.FileUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.recommend.service.RecommendExcelAndDownloadService;
import com.abtnetworks.totems.recommend.utils.ExportExcelUtils;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @desc    ??????????????????
 * @author zhoumuhua
 * @date 2021-07-12
 */
@Api(tags ="????????????????????????")
@RestController
@RequestMapping(value="/autoExcel")
@Slf4j
public class PushAutoRecommendExcelController {

    @Value("${resourceHandler}")
    String resourceHandler;

    @Value("${importAutoRecommendExcelFile}")
    String importAutoRecommendExcelFile;

    @Value("${importPortectNetworkExcelFile}")
    String importPortectNetworkExcelFile;

    @Autowired
    ExcelParser generateExcelParser;

    @Autowired
    private PushAutoRecommendService pushAutoRecommendService;

    @Autowired
    @Qualifier(value = "autoGenerateExecutor")
    private Executor autoGenerateExecutor;

    @Autowired
    private PushProtectNetworkConfigService pushProtectNetworkConfigService;

    @Autowired
    private PushAutoRecommendExcelService pushAutoRecommendExcelService;

    @Resource
    RecommendExcelAndDownloadService recommendExcelAndDownloadService;

    @Value("${push.download-file}")
    String dirPath;


    @ApiOperation(value = "????????????????????????", httpMethod = "POST", notes = "????????????Excel???????????????????????????")
    @PostMapping("/task/autorecommendimport")
    public JSONObject importPolicyGenerate(MultipartFile file, Authentication auth, HttpServletResponse response) {
        log.info("????????????????????????");
        String status = "-1";
        String errcode = "";
        String msg = "";
        JSONObject jsonObject = new JSONObject();
        String userName = auth.getName();
        List<AutoRecommendTaskVO> tmpList = new ArrayList<>();
        //??????????????????
        long start = System.currentTimeMillis();
        log.info("????????????excel...,???????????????{}???",start);
        long end = System.currentTimeMillis();
        long consume = end - start;
        msg = generateExcelParser.parseAutoRecommendExcel(file, userName, tmpList);
        log.info("??????excel??????...,???????????????{}???,?????????{}?????????",end,consume);
        if (!AliStringUtils.isEmpty(msg)) {
            return returnJSON(status, jsonObject, errcode, msg,"import");
        }
        //????????????????????????????????????

        log.info("????????????????????????????????????{}",tmpList.size());
        CountDownLatch latch = new CountDownLatch(tmpList.size());
        for (AutoRecommendTaskVO entity : tmpList) {
//            entity.setSrcInputType(0);
//            entity.setDstInputType(2);
            //String id = "auto_import_" +  DateUtil.getTimeStamp() + IdGen.getRandomNumberString(2);
            String id = "auto_import_" +  entity.getTheme() + IdGen.getRandomNumberString(2);
            if (ExtendedExecutor.containsKey(id)) {
                log.info(String.format("????????????????????????(%s)????????????????????????????????????", id));
                latch.countDown();
                continue;
            }

            // ???????????????????????????????????????
            autoGenerateExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "????????????????????????", "", new Date()), latch) {
                @Override
                protected void start() throws InterruptedException, Exception {
                    pushAutoRecommendService.addTask(entity);
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
        end = System.currentTimeMillis();
        consume = end - start;
        log.info("????????????...,???????????????{}???,?????????{}?????????",end,consume);
        status = "0";
        return returnJSON(status, jsonObject, errcode, msg,"import");
    }

    @ApiOperation("????????????Excel????????????")
    @PostMapping("/task/downloadAutoTemplate")
    public JSONObject downloadAutoRecommendTemplate() {
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", resourceHandler.replace("**", "") + importAutoRecommendExcelFile);
            status = "0";
        } catch (Exception e) {
            log.error("downloadAutoTemplate???" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg,null);
    }

    @ApiOperation(value = "????????????????????????", httpMethod = "POST", notes = "????????????Excel??????????????????????????????")
    @PostMapping("/task/protectimport")
    public JSONObject importProtectNetworkGenerate(MultipartFile file, Authentication auth, HttpServletResponse response) {
        log.info("????????????????????????");
        String status = "-1";
        String errcode = "";
        String msg = "";
        JSONObject jsonObject = new JSONObject();
        String userName = auth.getName();
        List<ProtectNetworkConfigVO> tmpList = new ArrayList<>();
        //??????????????????
        long start = System.currentTimeMillis();
        log.info("????????????excel...,???????????????{}???",start);
        long end = System.currentTimeMillis();
        long consume = end - start;
        msg = pushAutoRecommendExcelService.parseProtectNetworkExcel(file, userName, tmpList);
        log.info("??????excel??????...,???????????????{}???,?????????{}?????????",end,consume);
        if (!AliStringUtils.isEmpty(msg)) {
            return returnJSON(status, jsonObject, errcode, msg,"import");
        }
        //????????????????????????????????????

        log.info("??????????????????????????????????????????{}",tmpList.size());
        CountDownLatch latch = new CountDownLatch(tmpList.size());
        for (ProtectNetworkConfigVO entity : tmpList) {

            String id = "protect_import_" + entity.getDeviceIp()  + IdGen.getRandomNumberString(2);
            if (ExtendedExecutor.containsKey(id)) {
                log.info(String.format("????????????????????????(%s)????????????????????????????????????", id));
                latch.countDown();
                continue;
            }

            // ???????????????????????????????????????
            autoGenerateExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "????????????????????????", "", new Date()), latch) {
                @Override
                protected void start() throws InterruptedException, Exception {
                    pushProtectNetworkConfigService.addOrUpdateConfig(entity);
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
        end = System.currentTimeMillis();
        consume = end - start;
        log.info("????????????...,???????????????{}???,?????????{}?????????",end,consume);
        status = "0";
        return returnJSON(status, jsonObject, errcode, msg,"import");
    }

    @ApiOperation("??????????????????????????????Excel????????????")
    @PostMapping("/task/downloadprotectTemplate")
    public JSONObject downloadProtectNetworkTemplate() {
        String status = "-1";
        String errcode = "";
        String errmsg = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fileName", resourceHandler.replace("**", "") + importPortectNetworkExcelFile);
            status = "0";
        } catch (Exception e) {
            log.error("downloadAutoTemplate???" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg,null);
    }

    @ApiOperation(value = "????????????excel??????")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "??????????????????", required = false, dataType = "String")
    })
    @ApiResponses(value = {@ApiResponse(code = 200, message = "")})
    @RequestMapping(value = "task/exportprotect", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload) throws Exception {
        ProtectNetworkConfigSearchVO vo = new ProtectNetworkConfigSearchVO();
        List<ProtectNetworkConfigEntity> protectList = pushProtectNetworkConfigService.findExcelList(vo);

        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "??????????????????";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("??????????????????????????????????????????", e1);
        }

        String destDirName = dirPath + "/protectNetworkExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "protectNetworkdoing.temp";

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
                    new ProtectNetworkExportThread(filePath, doingFile, protectList).start();
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
                new ProtectNetworkExportThread(filePath, doingFile, protectList).start();
                resultRO.setMessage("??????????????????");
                jsonObject.put("filePath", preFilename + ".xlsx");
                jsonObject.put("status", 2);
                resultRO.setData(jsonObject);
                return resultRO;
            } else {
                pushAutoRecommendExcelService.downLoadProtect(response, destDirName + "/" + fileIsExistsName);
                return null;
            }
        } catch (Exception e) {
            File doingFile = new File(doingFileTemp);
            doingFile.delete();
            log.error("??????????????????Excel????????????:", e);
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
    private class ProtectNetworkExportThread extends Thread {
        private String filePath;
        private File doingFile;
        private List<ProtectNetworkConfigEntity> protectList;

        public ProtectNetworkExportThread(String filePath, File doingFile, List<ProtectNetworkConfigEntity> protectList) {
            super();
            this.filePath = filePath;
            this.doingFile = doingFile;
            this.protectList = protectList;
        }

        public ProtectNetworkExportThread() {
            super();
        }

        @Override
        public void run() {
            OutputStream out = null;
            try {
                out = new FileOutputStream(filePath);

                String[] protectHeaders = { "??????IP", "????????????", "????????????", "????????????NAT??????"};
                List<List<String>> protectData = new ArrayList<List<String>>();
                String[] natHeaders = { "??????IP", "??????", "?????????IP", "?????????IP", "??????", "???????????????", "???????????????"};
                List<List<String>> natDatas = new ArrayList<List<String>>();
                for(ProtectNetworkConfigEntity protect : protectList){
                    List<String> rowData = new ArrayList<>();
                    rowData.add(protect.getDeviceIp());
                    rowData.add(protect.getDeviceName());
                    rowData.add(getProtectView(protect.getProtectNetwork()));
                    rowData.add(getPushNatFlagView(protect.getNatFlag()));
                    protectData.add(rowData);

                    if (PushNatFlagEnum.NAT_FLAG_Y.getCode().equals(protect.getNatFlag())) {
                        List<ProtectNetworkNatMappingEntity> natMappingDTOList = protect.getNatMappingDTOList();
                        for (ProtectNetworkNatMappingEntity natMapping : natMappingDTOList) {
                            List<String> natData = new ArrayList<>();
                            natData.add(protect.getDeviceIp());
                            natData.add(PushNatTypeEnum.getDescByCode(natMapping.getNatType()));
                            natData.add(natMapping.getOutsideIp());
                            natData.add(natMapping.getInsideIp());
                            natData.add(natMapping.getOutsideProtocol());
                            natData.add(natMapping.getOutsidePorts());
                            natData.add(natMapping.getInsidePorts());
                            natDatas.add(natData);
                        }
                    }
                }
                ExportExcelUtils eeu = new ExportExcelUtils();
                XSSFWorkbook workbook = new XSSFWorkbook();
                eeu.exportCommonData(workbook, 0, "??????????????????", protectHeaders, protectData, out);
                eeu.exportCommonData(workbook, 1, "????????????nat????????????", natHeaders, natDatas, out);

                // ????????????????????????????????????????????????????????????????????????
                workbook.write(out);
            } catch (Exception e) {
                File file = new File(filePath);
                if(file.exists()) {
                    file.delete();
                }
                log.error("??????????????????excel??????", e);
            }finally {
                try{
                    if(out != null) {
                        out.close();
                    }
                }catch (IOException e1){
                    log.error("???????????????");
                }
            }
            doingFile.delete();
        }
    }

    /**
     * ??????nat??????????????????
     * @param natFlag
     * @return
     */
    private String getPushNatFlagView (String natFlag) {
        if (PushNatFlagEnum.NAT_FLAG_Y.getCode().equals(natFlag)) {
            return "???";
        }
        return "???";
    }

    /**
     * ??????????????????????????????
     * @param protectStr
     * @return
     */
    private String getProtectView (String protectStr) {
        if (StringUtils.isEmpty(protectStr)) {
            return "";
        }
        return protectStr.replaceAll(",","\n");
    }


    /**
     * ?????????
     * @param status
     * @param data
     * @param errcode
     * @param errmsg
     * @param type //???import???????????????code???Msg
     * @return
     */
    @ResponseBody
    public <T>JSONObject returnJSON (String status, T data, String errcode, String errmsg,String type) {

        JSONObject jsonObject = new JSONObject();
        if ("import".equals(type)) {
            jsonObject.put("code",StringUtils.isEmpty(status) ? "0" : status);
            jsonObject.put("msg",errmsg);
        } else {
            jsonObject.put("status", StringUtils.isEmpty(status) ? "0" : status);
            jsonObject.put("errmsg",errmsg);
        }
        jsonObject.put("data",data);
        jsonObject.put("errcode",errcode);

        return jsonObject;
    }

    @ApiOperation(value = "????????????excel??????", httpMethod = "GET")
    @RequestMapping(value = "task/export", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload, AutoRecommendTaskSearchVO vo, Authentication authentication) throws FileNotFoundException {

        List<AutoRecommendTaskEntity> list = pushAutoRecommendService.selectList(vo);
        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "??????????????????";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("??????????????????????????????????????????", e1);
        }
        String destDirName = dirPath + "/pushAutoGenerateExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "pushAutoGeneratedoing.temp";

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
                    new PushAutoGenerateThread(filePath, doingFile, authentication, pushAutoRecommendService, list).start();
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
                new PushAutoGenerateThread(filePath, doingFile, authentication, pushAutoRecommendService, list).start();
                resultRO.setMessage("??????????????????");
                jsonObject.put("filePath", preFilename + ".xlsx");
                jsonObject.put("status", 2);
                resultRO.setData(jsonObject);
                return resultRO;
            } else {
                recommendExcelAndDownloadService.downLoadPolicyAdd(response, destDirName + "/" + fileIsExistsName);
                return null;
            }
        } catch (Exception e) {
            File doingFile = new File(doingFileTemp);
            doingFile.delete();
            log.error("??????????????????Excel????????????:", e);
            resultRO.setMessage("??????????????????");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }
}
