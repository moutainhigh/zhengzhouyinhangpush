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
 * @desc    自动开通工单
 * @author zhoumuhua
 * @date 2021-07-12
 */
@Api(tags ="自动开通导入导出")
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


    @ApiOperation(value = "批量自动开通导入", httpMethod = "POST", notes = "根据导入Excel表，批量生成命令行")
    @PostMapping("/task/autorecommendimport")
    public JSONObject importPolicyGenerate(MultipartFile file, Authentication auth, HttpServletResponse response) {
        log.info("批量导入自动开通");
        String status = "-1";
        String errcode = "";
        String msg = "";
        JSONObject jsonObject = new JSONObject();
        String userName = auth.getName();
        List<AutoRecommendTaskVO> tmpList = new ArrayList<>();
        //投标使用日志
        long start = System.currentTimeMillis();
        log.info("开始解析excel...,开始时间【{}】",start);
        long end = System.currentTimeMillis();
        long consume = end - start;
        msg = generateExcelParser.parseAutoRecommendExcel(file, userName, tmpList);
        log.info("解析excel结束...,结束时间【{}】,耗时【{}】毫秒",end,consume);
        if (!AliStringUtils.isEmpty(msg)) {
            return returnJSON(status, jsonObject, errcode, msg,"import");
        }
        //记录到数据库并生成命令行

        log.info("本次批量导入工单数量为：{}",tmpList.size());
        CountDownLatch latch = new CountDownLatch(tmpList.size());
        for (AutoRecommendTaskVO entity : tmpList) {
//            entity.setSrcInputType(0);
//            entity.setDstInputType(2);
            //String id = "auto_import_" +  DateUtil.getTimeStamp() + IdGen.getRandomNumberString(2);
            String id = "auto_import_" +  entity.getTheme() + IdGen.getRandomNumberString(2);
            if (ExtendedExecutor.containsKey(id)) {
                log.info(String.format("自动开通导入任务(%s)已经存在！任务不重复添加", id));
                latch.countDown();
                continue;
            }

            // 异步处理命令行生成入库流程
            autoGenerateExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "批量导入自动开通", "", new Date()), latch) {
                @Override
                protected void start() throws InterruptedException, Exception {
                    pushAutoRecommendService.addTask(entity);
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            log.error("自动开通工单任务导入异常", e);
        }
        end = System.currentTimeMillis();
        consume = end - start;
        log.info("导入结束...,结束时间【{}】,耗时【{}】毫秒",end,consume);
        status = "0";
        return returnJSON(status, jsonObject, errcode, msg,"import");
    }

    @ApiOperation("下载导入Excel任务模板")
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
            log.error("downloadAutoTemplate：" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg,null);
    }

    @ApiOperation(value = "防护网段批量导入", httpMethod = "POST", notes = "根据导入Excel表，批量添加防护网段")
    @PostMapping("/task/protectimport")
    public JSONObject importProtectNetworkGenerate(MultipartFile file, Authentication auth, HttpServletResponse response) {
        log.info("批量导入防护网段");
        String status = "-1";
        String errcode = "";
        String msg = "";
        JSONObject jsonObject = new JSONObject();
        String userName = auth.getName();
        List<ProtectNetworkConfigVO> tmpList = new ArrayList<>();
        //投标使用日志
        long start = System.currentTimeMillis();
        log.info("开始解析excel...,开始时间【{}】",start);
        long end = System.currentTimeMillis();
        long consume = end - start;
        msg = pushAutoRecommendExcelService.parseProtectNetworkExcel(file, userName, tmpList);
        log.info("解析excel结束...,结束时间【{}】,耗时【{}】毫秒",end,consume);
        if (!AliStringUtils.isEmpty(msg)) {
            return returnJSON(status, jsonObject, errcode, msg,"import");
        }
        //记录到数据库并生成命令行

        log.info("本次批量导入防护网段数量为：{}",tmpList.size());
        CountDownLatch latch = new CountDownLatch(tmpList.size());
        for (ProtectNetworkConfigVO entity : tmpList) {

            String id = "protect_import_" + entity.getDeviceIp()  + IdGen.getRandomNumberString(2);
            if (ExtendedExecutor.containsKey(id)) {
                log.info(String.format("防护网段导入任务(%s)已经存在！任务不重复添加", id));
                latch.countDown();
                continue;
            }

            // 异步处理命令行生成入库流程
            autoGenerateExecutor.execute(new ExtendedLatchRunnable(new ExecutorDto(id, "批量导入防护网段", "", new Date()), latch) {
                @Override
                protected void start() throws InterruptedException, Exception {
                    pushProtectNetworkConfigService.addOrUpdateConfig(entity);
                }
            });
        }

        try {
            latch.await();
        } catch (Exception e) {
            log.error("防护网段导入异常", e);
        }
        end = System.currentTimeMillis();
        consume = end - start;
        log.info("导入结束...,结束时间【{}】,耗时【{}】毫秒",end,consume);
        status = "0";
        return returnJSON(status, jsonObject, errcode, msg,"import");
    }

    @ApiOperation("下载批量导入防护网段Excel任务模板")
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
            log.error("downloadAutoTemplate：" + e);
        }

        return returnJSON(status, jsonObject, errcode, errmsg,null);
    }

    @ApiOperation(value = "防护网段excel导出")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "isReload", value = "是否重新下载", required = false, dataType = "String")
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
        String preFilename = "防护网段导出";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("生成防护网段报表文件名称异常", e1);
        }

        String destDirName = dirPath + "/protectNetworkExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "protectNetworkdoing.temp";

        try {
            // 生成策略开通文件夹
            if (!new File(destDirName).exists()) {
                FileUtils.createDir(destDirName);
            }

            String fileIsExistsName = FileUtils.isDirExistFile(destDirName);
            boolean doingFileTempIsExists = FileUtils.fileIsExists(doingFileTemp);
            boolean fileIsExists = FileUtils.fileIsExists(destDirName + "/" + fileIsExistsName);
            if (null == isReload) {
                if (fileIsExists && doingFileTempIsExists == false) {
                    resultRO.setMessage("文件生成成功");
                    jsonObject.put("filePath", fileIsExistsName);
                    jsonObject.put("status", 1);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists) {
                    // 有正在生成的临时文件
                    resultRO.setMessage("文件生成中");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists == false && fileIsExists == false) {
                    // 生成临时文件
                    File doingFile = new File(doingFileTemp);
                    doingFile.createNewFile();
                    resultRO.setMessage("生成成功");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    new ProtectNetworkExportThread(filePath, doingFile, protectList).start();
                    resultRO.setData(jsonObject);
                    return resultRO;
                }
            }
            if ("true".equals(isReload)) {
                // 生成临时文件
                FileUtils.deleteFileByPath(destDirName + "/" + fileIsExistsName);
                // 不存在获取数据从新下载
                // 生成临时文件
                File doingFile = new File(doingFileTemp);
                doingFile.createNewFile();
                new ProtectNetworkExportThread(filePath, doingFile, protectList).start();
                resultRO.setMessage("正在生成文件");
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
            log.error("下载防护网段Excel表格失败:", e);
            resultRO.setMessage("数据导出失败");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }

    /**
     * 节点配置导出线程启动类,用于生成excel
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

                String[] protectHeaders = { "设备IP", "设备名称", "防护网段", "是否存在NAT映射"};
                List<List<String>> protectData = new ArrayList<List<String>>();
                String[] natHeaders = { "设备IP", "类型", "转换前IP", "转换后IP", "协议", "转换前端口", "转换后端口"};
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
                eeu.exportCommonData(workbook, 0, "防护网段信息", protectHeaders, protectData, out);
                eeu.exportCommonData(workbook, 1, "防护网段nat映射信息", natHeaders, natDatas, out);

                // 原理就是将所有的数据一起写入，然后再关闭输入流。
                workbook.write(out);
            } catch (Exception e) {
                File file = new File(filePath);
                if(file.exists()) {
                    file.delete();
                }
                log.error("防护网段导出excel异常", e);
            }finally {
                try{
                    if(out != null) {
                        out.close();
                    }
                }catch (IOException e1){
                    log.error("关闭流异常");
                }
            }
            doingFile.delete();
        }
    }

    /**
     * 获取nat映射导出显示
     * @param natFlag
     * @return
     */
    private String getPushNatFlagView (String natFlag) {
        if (PushNatFlagEnum.NAT_FLAG_Y.getCode().equals(natFlag)) {
            return "是";
        }
        return "否";
    }

    /**
     * 获取防护网段导出显示
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
     * 返回值
     * @param status
     * @param data
     * @param errcode
     * @param errmsg
     * @param type //为import时返回前台code和Msg
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

    @ApiOperation(value = "自动开通excel导出", httpMethod = "GET")
    @RequestMapping(value = "task/export", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public ResultRO<JSONObject> download(HttpServletResponse response, String isReload, AutoRecommendTaskSearchVO vo, Authentication authentication) throws FileNotFoundException {

        List<AutoRecommendTaskEntity> list = pushAutoRecommendService.selectList(vo);
        ResultRO<JSONObject> resultRO = new ResultRO(true);
        JSONObject jsonObject = new JSONObject();
        String standardDateTime = DateUtil.getTimeStamp();
        String preFilename = "自动开通工单";
        try {
            preFilename = new String(preFilename.getBytes("UTF-8"), "UTF-8");
            preFilename += "_" + standardDateTime;
        } catch (UnsupportedEncodingException e1) {
            log.error("生成策略生成报表文件名称异常", e1);
        }
        String destDirName = dirPath + "/pushAutoGenerateExcel";
        String filePath = destDirName + "/" + preFilename + ".xlsx";
        String doingFileTemp = destDirName + "/" + "pushAutoGeneratedoing.temp";

        try {
            // 生成策略开通文件夹
            if (!new File(destDirName).exists()) {
                FileUtils.createDir(destDirName);
            }

            String fileIsExistsName = FileUtils.isDirExistFile(destDirName);
            boolean doingFileTempIsExists = FileUtils.fileIsExists(doingFileTemp);
            boolean fileIsExists = FileUtils.fileIsExists(destDirName + "/" + fileIsExistsName);
            if (null == isReload) {
                if (fileIsExists && doingFileTempIsExists == false) {
                    resultRO.setMessage("文件生成成功");
                    jsonObject.put("filePath", fileIsExistsName);
                    jsonObject.put("status", 1);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists) {
                    // 有正在生成的临时文件
                    resultRO.setMessage("文件生成中");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    resultRO.setData(jsonObject);
                    return resultRO;
                } else if (doingFileTempIsExists == false && fileIsExists == false) {
                    // 生成临时文件
                    File doingFile = new File(doingFileTemp);
                    doingFile.createNewFile();
                    resultRO.setMessage("生成成功");
                    jsonObject.put("filePath", preFilename + ".xlsx");
                    jsonObject.put("status", 2);
                    new PushAutoGenerateThread(filePath, doingFile, authentication, pushAutoRecommendService, list).start();
                    resultRO.setData(jsonObject);
                    return resultRO;
                }
            }
            if ("true".equals(isReload)) {
                // 生成临时文件
                FileUtils.deleteFileByPath(destDirName + "/" + fileIsExistsName);
                // 不存在获取数据从新下载
                // 生成临时文件
                File doingFile = new File(doingFileTemp);
                doingFile.createNewFile();
                new PushAutoGenerateThread(filePath, doingFile, authentication, pushAutoRecommendService, list).start();
                resultRO.setMessage("正在生成文件");
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
            log.error("下载策略概览Excel表格失败:", e);
            resultRO.setMessage("数据导出失败");
            resultRO.setSuccess(false);
            jsonObject.put("filePath", filePath);
            jsonObject.put("status", 3);
            resultRO.setData(jsonObject);
        }
        return resultRO;
    }
}
