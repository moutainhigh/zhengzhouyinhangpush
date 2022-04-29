package com.abtnetworks.totems.recommend.vo.excel;

import lombok.Data;

/**
 * @author Administrator
 * @Title:
 * @Description: 批量采集工单信息返回
 * @date 2021/1/26
 */
@Data
public class ExcelImportResultVO {
    /**批量导入工单号**/
    private Integer resultCode;
    /**批量导入工单名**/
    private String orderNumber;
}
