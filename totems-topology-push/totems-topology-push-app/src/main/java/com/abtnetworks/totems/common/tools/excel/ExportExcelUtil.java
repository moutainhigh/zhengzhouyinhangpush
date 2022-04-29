package com.abtnetworks.totems.common.tools.excel;

import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;


/**
 * 导出Excel工具类 2007
 * @author myl
 *
 * @param
 */
public class ExportExcelUtil {



    /**
     * <p>
     * 导出带有头部标题行的Excel <br>
     * 时间格式默认：yyyy-MM-dd hh:mm:ss <br>
     * </p>
     *
     * @param title 表格标题
     * @param headers 头部标题集合
     * @param contents 属性集合
     * @param dataset 数据集合
     * @param out 输出流
     */
    @SneakyThrows
    public static void exportExcel(String title, String[] headers, String[] contents, List dataset, OutputStream out) {
        // 声明一个工作薄
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {

            SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook, -1);
            ExportExcelBaseUtil<Object> util = new ExportExcelBaseUtil<>();
            util.exportExcel(sxssfWorkbook, title, headers, contents, dataset, false, false, out);
            sxssfWorkbook.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            workbook.close();
        }
    }

}
