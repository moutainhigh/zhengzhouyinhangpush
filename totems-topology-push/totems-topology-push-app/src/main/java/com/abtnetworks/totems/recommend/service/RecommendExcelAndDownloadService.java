package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.common.ReturnResult;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskSpecialNatEntity;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskStaticRouteEntity;
import com.abtnetworks.totems.recommend.dto.excel.DataRecommendForExcelDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelBigInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendInternetTaskDTO;
import com.abtnetworks.totems.generate.dto.excel.ExcelTaskNatEntity;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.recommend.vo.excel.ExcelImportResultVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/14
 */
public interface RecommendExcelAndDownloadService {

    /**时间格式**/
    String DATE_FORMAT = "yyyy/MM/dd HH:mm";

    /**
     * 检查校验excel
     * @param natExcelList
     * @param filename
     * @return
     */
    String checkExcelNatTaskValidation(List<ExcelTaskNatEntity> natExcelList,String filename);

    /**
     * 检查特殊NAT场景excel
     * @param specialNatExcelList
     * @return
     */
    String checkExcelSpecialNatValidation(List<ExcelTaskSpecialNatEntity> specialNatExcelList);


    /**
     * 检查校验静态路由excel
     * @param routeExcelList
     * @return
     */
    String checkExcelRouteTaskValidation(List<ExcelTaskStaticRouteEntity> routeExcelList);

    /**
     * 策略开通下载
     * @param response
     * @param fileExcelPath
     */
    void downLoadPolicyAdd(HttpServletResponse response, String fileExcelPath);

    /**
     * 将excel的数据互联网开通保存策略到数据库（先实现一个）
     * @param dataRecommendForExcelDTO
     */
    ExcelImportResultVO savePolicyDataForExcel(DataRecommendForExcelDTO dataRecommendForExcelDTO, SimpleDateFormat simpleDateFormat,Authentication auth);

    /**
     * 将excel的数据互联网开通保存策略到数据库（先实现一个）
     * @param dataRecommendForExcelDTO
     */
    ExcelImportResultVO saveBigInternetPolicyDataForExcel(DataRecommendForExcelDTO dataRecommendForExcelDTO, SimpleDateFormat simpleDateFormat);

    /***
     * 将不同类型的开通从excel中解析完成，之后转成实体
     * @param entity
     * @param excelRecommendInternetTaskDTO
     * @param taskType
     * @return
     */
    RecommendTaskEntity toCommonTaskEntity(ExcelBigInternetTaskDTO entity, ExcelRecommendInternetTaskDTO excelRecommendInternetTaskDTO, Integer taskType,DataRecommendForExcelDTO dataRecommendForExcelDTO) throws ParseException;

    /**
     * 当获取不到的时候就用auth
     * @param creator
     * @param auth
     * @return
     */
    UserInfoDTO getUserInfo(String creator, Authentication auth);

    /**
     * 将文件放入仿真导入历史列表
     * @param file
     * @param fileName
     * @param createdUser
     * @param date
     * @return
     */
    ReturnResult<String> recodeFileToHistory(MultipartFile file, String fileName, String createdUser, Date date);
}
