package com.abtnetworks.totems.recommend.dto.excel;

import com.abtnetworks.totems.branch.dto.UserInfoDTO;
import com.abtnetworks.totems.recommend.entity.RecommendTaskEntity;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/1/19
 */
@Data
public class DataRecommendForExcelDTO {

    /***判断是否有新的错误信息**/
    private Boolean hasInvalid;

    /***互联网开通内外，外内**/
    private StringBuilder failureMsg;

    /***互联网开通内外，外内**/
    private List<ExcelRecommendInternetTaskDTO> internetExcelDataList;
    /***文件名**/
    private String filename;
    /***申请人**/
    private String creator;

    /***模拟NAT开通有数据，既需要进行模拟变更场景设置**/
    private WhatIfRO whatIf;
    /***用户信息**/
    private UserInfoDTO userInfoDTO;
    /***五元组信息去重方式**/
    private Set<String> taskStringSet;
    /**所有仿真的工单（安全策略、）**/
    private List<RecommendTaskEntity> tmpList;
    /**重复的策略**/
    private List<RecommendTaskEntity> reduplicatedList;
    /**记录错误数量**/
    private Integer failureNum;
    /**开通类型**/
    private Integer taskType;

    /****大网段开通**/
    List<ExcelBigInternetTaskDTO> excelBigInternetTaskDTOS;


}
