package com.abtnetworks.totems.common.tools.excel;



import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 导出Excel基础类 2007
 * @author myl
 *
 * @param <T>
 */
public class ExportExcelBaseUtil<T> {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * <p>
     * 导出带有头部标题行的Excel <br>
     * 时间格式默认：yyyy-MM-dd hh:mm:ss <br>
     * </p>
     *
     * @param sxssfWorkbook 工作薄
     * @param title 表格标题
     * @param headers 头部标题集合
     * @param contents 属性集合
     * @param dataset 数据集合
     * @param isMerge 是否合并单元格
     * @param out 输出流
     */
    public void exportExcel(SXSSFWorkbook sxssfWorkbook, String title,String[] headers, String[] contents, Collection<T> dataset, boolean isMerge, boolean whetherStr, OutputStream out) {
        if (contents != null){
            if (whetherStr){
                exportMapStrExcel2007(sxssfWorkbook, title, headers, contents, dataset, isMerge, out, "yyyy-MM-dd hh:mm:ss");
            }else {
                exportExcel2007(sxssfWorkbook, title, headers, contents, dataset, isMerge, out, "yyyy-MM-dd hh:mm:ss");
            }
        }else{
            exportExcel2007(sxssfWorkbook, title, headers, dataset, out, "yyyy-MM-dd hh:mm:ss");
        }
    }

    /**
     * <p>
     * 通用Excel导出方法,利用反射机制遍历对象的所有字段，将数据写入Excel文件中 <br>
     * 此版本生成2007以上版本的文件 (文件后缀：xlsx)
     * </p>
     *
     * @param sxssfWorkbook
     *            工作薄
     * @param title
     *            表格标题名
     * @param headers
     *            表格头部标题集合
     * @param dataset
     *            需要显示的数据集合,集合中一定要放置符合JavaBean风格的类的对象。此方法支持的
     *            JavaBean属性的数据类型有基本数据类型及String,Date
     * @param out
     *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern
     *            如果有时间数据，设定输出格式。默认为"yyyy-MM-dd hh:mm:ss"
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exportExcel2007(SXSSFWorkbook sxssfWorkbook, String title, String[] headers, Collection<T> dataset, OutputStream out, String pattern) {
        // 生成一个表格
        Sheet sheet = sxssfWorkbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(25);
        // 生成一个样式
        CellStyle style = sxssfWorkbook.createCellStyle();
        // 设置这些样式

        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        // 生成一个字体
        Font font = sxssfWorkbook.createFont();
//        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 11);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式
        CellStyle style2 = sxssfWorkbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.WHITE.index);
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
//        style2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style2.setWrapText(true);
        // 生成另一个字体
        Font font2 = sxssfWorkbook.createFont();
//        font2.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);
        font2.setBold(true);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 产生表格标题行
        Row row = sheet.createRow(0);
        Cell cellHeader;
        for (int i = 0; i < headers.length; i++) {
            cellHeader = row.createCell(i);
            cellHeader.setCellStyle(style);
            cellHeader.setCellValue(new XSSFRichTextString(headers[i]));
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        T t;
        Field[] fields;
        Field field;
        XSSFRichTextString richString;
        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
        Matcher matcher;
        String fieldName;
        String getMethodName;
        Cell cell;
        Class tCls;
        Method getMethod;
        Object value;
        String textValue;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            t = (T) it.next();
            // 利用反射，根据JavaBean属性的先后顺序，动态调用getXxx()方法得到属性值
            fields = t.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(style2);
                field = fields[i];
                fieldName = field.getName();
                getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                try {
                    tCls = t.getClass();
                    getMethod = tCls.getMethod(getMethodName, new Class[] {});
                    value = getMethod.invoke(t, new Object[] {});
                    // 判断值的类型后进行强制类型转换
                    textValue = null;
                    if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else if (value instanceof Float) {
                        textValue = String.valueOf((Float) value);
                        cell.setCellValue(textValue);
                    } else if (value instanceof Double) {
                        textValue = String.valueOf((Double) value);
                        cell.setCellValue(textValue);
                    } else if (value instanceof Long) {
                        cell.setCellValue((Long) value);
                    } else if (value instanceof Boolean) {
                        textValue = "是";
                        if (!(Boolean) value) {
                            textValue = "否";
                        }
                    } else if (value instanceof Date) {
                        textValue = sdf.format((Date) value);
                    } else {
                        // 其它数据类型都当作字符串简单处理
                        if (value != null) {
                            textValue = value.toString();
                        }
                    }
                    if (textValue != null) {
                        matcher = p.matcher(textValue);
                        if (matcher.matches()) {
                            // 是数字当作double处理
                            cell.setCellValue(Double.parseDouble(textValue));
                        } else {
                            richString = new XSSFRichTextString(textValue);
                            cell.setCellValue(richString);
                        }
                    }
                } catch (SecurityException e) {
                    logger.error(e.getMessage(),e);
                } catch (NoSuchMethodException e) {
                    logger.error(e.getMessage(),e);
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(),e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(),e);
                } finally {
                    // 清理资源
                }
            }
        }
    }

    /**
     * <p>
     * 通用Excel导出方法,导出对象的contents字段，将数据写入Excel文件中 <br>
     * 此版本生成2007以上版本的文件 (文件后缀：xlsx)
     * </p>
     *
     * @param sxssfWorkbook
     *            工作薄
     * @param title
     *            表格标题名
     * @param headers
     *            表格头部标题集合
     * @param contents
     *            bean属性名
     * @param dataset
     *            需要显示的数据集合,集合中一定要放置符合JavaBean风格的类的对象。此方法支持的
     *            JavaBean属性的数据类型有基本数据类型及String,Date
     * @param isMerge 是否合并单元格
     * @param out
     *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern
     *            如果有时间数据，设定输出格式。默认为"yyyy-MM-dd hh:mm:ss"
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exportExcel2007(SXSSFWorkbook sxssfWorkbook, String title, String[] headers, String[] contents, Collection<T> dataset, boolean isMerge, OutputStream out, String pattern) {
        // 生成一个表格
        Sheet sheet = sxssfWorkbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(25);
        // 生成一个样式
        CellStyle style = sxssfWorkbook.createCellStyle();
        // 设置这些样式



        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setWrapText(true);
        // 生成一个字体
        Font font = sxssfWorkbook.createFont();
//        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 11);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式
        CellStyle style2 = sxssfWorkbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.WHITE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
//        style2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style2.setWrapText(true);
        // 生成另一个字体
        Font font2 = sxssfWorkbook.createFont();
//        font2.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 产生表格标题行
        Row row = sheet.createRow(0);
        Cell cellHeader;
        for (int i = 0; i < headers.length; i++) {
            cellHeader = row.createCell(i);
            cellHeader.setCellStyle(style);
            cellHeader.setCellValue(new XSSFRichTextString(headers[i]));
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        T t;
        Field[] fields;
        Field field;
        XSSFRichTextString richString;
        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
        Matcher matcher;
        String fieldName;
        String getMethodName;
        Cell cell;
        Class tCls;
        Method getMethod;
        Object value;
        String textValue;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            t = (T) it.next();
            // 循环bean属性，调用getXxx()方法得到属性值
            for (int i = 0; i < contents.length; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(style2);
                fieldName = contents[i];
                getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                try {
                    tCls = t.getClass();
                    getMethod = tCls.getMethod(getMethodName, new Class[] {});
                    value = getMethod.invoke(t, new Object[] {});
                    // 判断值的类型后进行强制类型转换
                    textValue = null;
                    if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else if (value instanceof Float) {
                        textValue = String.valueOf((Float) value);
                        cell.setCellValue(textValue);
                    } else if (value instanceof Double) {
                        textValue = String.valueOf((Double) value);
                        cell.setCellValue(textValue);
                    } else if (value instanceof Long) {
                        cell.setCellValue((Long) value);
                    } else if (value instanceof Boolean) {
                        textValue = "是";
                        if (!(Boolean) value) {
                            textValue = "否";
                        }
                    } else if (value instanceof Date) {
                        textValue = sdf.format((Date) value);
                    } else {
                        // 其它数据类型都当作字符串简单处理
                        if (value != null) {
                            textValue = value.toString();
                        }
                    }
                    if (textValue != null) {
                        matcher = p.matcher(textValue);
                        if (matcher.matches()) {
                            // 是数字当作double处理
                            cell.setCellValue(Double.parseDouble(textValue));
                        } else {
                            richString = new XSSFRichTextString(textValue);
                            cell.setCellValue(richString);
                        }
                    }
                } catch (SecurityException e) {
                    logger.error(e.getMessage(),e);
                } catch (NoSuchMethodException e) {
                    logger.error(e.getMessage(),e);
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(),e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(),e);
                } finally {
                    // 清理资源
                }
            }
        }
        if(isMerge){
            // 合并单元格（合并空值单元格）
            if(sheet.getLastRowNum() > 1){
                int sk = 1;
                for (int a = 0; a < contents.length; a++) {
                    sk = 1;
                    for(int i=2;i<=sheet.getLastRowNum();i++){
                        Row rows = sheet.getRow(i);
                        Cell cell_0 = rows.getCell(a);
                        boolean b_r = false;
                        if (cell_0.getCellType() == CellType.STRING || cell_0.getCellType() == CellType.BLANK){
                            if("".equals(cell_0.getStringCellValue())){
                                b_r = true;
                            }
                        }
                        if(b_r){
                            if(i==sheet.getLastRowNum()){
                                sheet.addMergedRegion(new CellRangeAddress(sk, i, a, a));
                            }
                        }else{
                            if(sk != i-1){
                                sheet.addMergedRegion(new CellRangeAddress(sk, i-1, a, a));//起始行号，终止行号， 起始列号，终止列号
                            }
                            sk = i;
                        }
                    }
                }
            }
        }
    }



    /**
     * <p>
     * 通用Excel导出方法,导出对象的contents字段，将数据写入Excel文件中 <br>
     * 此版本生成2007以上版本的文件 (文件后缀：xlsx)
     * </p>
     *
     * @param sxssfWorkbook
     *            工作薄
     * @param title
     *            表格标题名
     * @param headers
     *            表格头部标题集合
     * @param contents
     *            bean属性名
     * @param dataset
     *            需要显示的数据集合,集合中一定要放置符合JavaBean风格的类的对象。此方法支持的
     *            JavaBean属性的数据类型有基本数据类型及String,Date
     * @param isMerge 是否合并单元格
     * @param out
     *            与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern
     *            如果有时间数据，设定输出格式。默认为"yyyy-MM-dd hh:mm:ss"
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exportMapStrExcel2007(SXSSFWorkbook sxssfWorkbook, String title, String[] headers, String[] contents, Collection<T> dataset, boolean isMerge, OutputStream out, String pattern) {
        // 生成一个表格
        Sheet sheet = sxssfWorkbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(25);
        // 生成一个样式
        CellStyle style = sxssfWorkbook.createCellStyle();
        // 设置这些样式




        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setWrapText(true);
        // 生成一个字体
        Font font = sxssfWorkbook.createFont();
//        font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 11);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式
        CellStyle style2 = sxssfWorkbook.createCellStyle();


//        style2.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//        style2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.index);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style2.setWrapText(true);
        // 生成另一个字体
        Font font2 = sxssfWorkbook.createFont();
//        font2.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 产生表格标题行
        Row row = sheet.createRow(0);
        Cell cellHeader;
        for (int i = 0; i < headers.length; i++) {
            cellHeader = row.createCell(i);
            cellHeader.setCellStyle(style);
            cellHeader.setCellValue(new XSSFRichTextString(headers[i]));
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        T t;
        Field[] fields;
        Field field;
        XSSFRichTextString richString;
        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
        Matcher matcher;
        String fieldName;
        String getMethodName;
        Cell cell;
        Class tCls;
        Method getMethod;
        Object value;
        String textValue;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        while (it.hasNext()) {
            t = (T) it.next();
            Map<String, List<String>> strListMap = (Map<String, List<String>>) t;
            for (String strK : strListMap.keySet()) {
                int u = 0;
                List<String> listV = strListMap.get(strK);
                for (String str : listV) {
                    index++;
                    row = sheet.createRow(index);
                    for (int i = 0; i < contents.length; i++) {

                        if (i==0){
                            value = "";
                        }else {
                            value = str;
                        }
                        if (u==0&&i==0){
                            value = strK;
                        }

                        cell = row.createCell(i);
                        cell.setCellStyle(style2);
                        try {

                            // 判断值的类型后进行强制类型转换
                            textValue = null;
                            // 其它数据类型都当作字符串简单处理
                            if (value != null) {
                                textValue = value.toString();
                            }
                            if (textValue != null) {
                                 richString = new XSSFRichTextString(textValue);
                                 cell.setCellValue(richString);
                            }
                        } catch (SecurityException e) {
                            logger.error(e.getMessage(),e);
                        } catch (IllegalArgumentException e) {
                            logger.error(e.getMessage(),e);
                        } finally {
                            // 清理资源
                        }
                    }
                    u++;
                }

            }

        }

        if(isMerge){
            // 合并单元格（合并空值单元格）
            if(sheet.getLastRowNum() > 1){
                int sk = 1;
                for (int a = 0; a < contents.length; a++) {
                    sk = 1;
                    for(int i=2;i<=sheet.getLastRowNum();i++){
                        Row rows = sheet.getRow(i);
                        Cell cell_0 = rows.getCell(a);
                        boolean b_r = false;
                        if (cell_0.getColumnIndex() == 1 || cell_0.getColumnIndex() == 3){
                            if("".equals(cell_0.getStringCellValue())){
                                b_r = true;
                            }
                        }

                        if(b_r){
                            if(i==sheet.getLastRowNum()){
                                sheet.addMergedRegion(new CellRangeAddress(sk, i, a, a));
                            }
                        }else{
                            if(sk != i-1){
                                sheet.addMergedRegion(new CellRangeAddress(sk, i-1, a, a));//起始行号，终止行号， 起始列号，终止列号
                            }
                            sk = i;
                        }

                    }
                }
            }
        }
    }

}
