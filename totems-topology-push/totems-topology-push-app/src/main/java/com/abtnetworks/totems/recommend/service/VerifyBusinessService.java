package com.abtnetworks.totems.recommend.service;

import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelBigInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendInternetTaskDTO;
import com.abtnetworks.totems.recommend.dto.verify.VerifyBussExcelDTO;
import com.abtnetworks.totems.recommend.dto.excel.ExcelRecommendTaskDTO;

import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 业务校验层
 * @date 2021/1/25
 */
public interface VerifyBusinessService {

    /**
     * 业务开通excel参数校验
     * @param entity
     * @return
     */
    VerifyBussExcelDTO verifyRecommendBussExcel(ExcelRecommendTaskDTO entity);

    /**
     * 大网段开通excel参数校验
     * @param entity
     * @return
     */
    VerifyBussExcelDTO verifyRecommendBigInternetExcel(ExcelBigInternetTaskDTO entity);

    /**
     * 互联网开通excel参数校验
     * @param entity
     * @return
     */
    VerifyBussExcelDTO verifyRecommendInternetExcel(ExcelRecommendInternetTaskDTO entity);

    /**
     * 服务校验与转化
     * @param service
     * @param serviceList
     * @param ipType
     * @return
     */
    int verifyService(String service, List<ServiceDTO> serviceList,Integer ipType);
}
