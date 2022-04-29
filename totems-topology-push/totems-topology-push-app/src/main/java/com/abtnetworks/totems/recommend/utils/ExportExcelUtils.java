package com.abtnetworks.totems.recommend.utils;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.OutputStream;
import java.util.List;

public class ExportExcelUtils {


	/**
	 * @Title: exportExcel
	 * @Description: 导出策略开通报表Excel的方法
	 * @param workbook
	 * @param sheetNum
	 *            (sheet的位置，0表示第一个表格中的第一个sheet)
	 * @param sheetTitle
	 *            （sheet的名称）
	 * @param headers
	 *            （表格的标题）
	 * @param result
	 *            （表格的数据）
	 * @param out
	 *            （输出流）
	 * @throws Exception
	 */
	public void exportPolicyAddData(XSSFWorkbook workbook, int sheetNum, String sheetTitle, String[] headers,
										 List<List<String>> result, OutputStream out) throws Exception {
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet();
		workbook.setSheetName(sheetNum, sheetTitle);
		// 设置表格默认列宽度为10个字节
		sheet.setDefaultColumnWidth((short) 10);
		// 生成一个样式
		XSSFCellStyle style = workbook.createCellStyle();
		//设置单元格颜色
		//style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// 设置这些样式
		XSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);// 设置字体大小
		sheet.setColumnWidth(0, 1766);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 4000);
		sheet.setColumnWidth(9, 4000);
		sheet.setColumnWidth(10, 4000);
		sheet.setColumnWidth(11, 4000);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(13, 4000);
		sheet.setColumnWidth(14, 4000);
		sheet.setColumnWidth(15, 4000);
		sheet.setColumnWidth(16, 4000);
		sheet.setColumnWidth(17, 4000);
		sheet.setColumnWidth(18, 4000);
		sheet.setColumnWidth(19, 4000);
		sheet.setColumnWidth(20, 4000);
		sheet.setColumnWidth(21, 4000);
		sheet.setColumnWidth(22, 4000);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		// 生成一个字体
		XSSFFont font1 = workbook.createFont();
		font1.setColor(IndexedColors.BLACK.getIndex());
		font1.setFontHeightInPoints((short) 12);
		font1.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);

		// 产生抬头行
		// 给抬头行生成一个样式
		XSSFCellStyle styleHead = workbook.createCellStyle();
		styleHead.setAlignment(HorizontalAlignment.CENTER);
		styleHead.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		styleHead.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHead.setBorderBottom(BorderStyle.THIN);
		styleHead.setBorderLeft(BorderStyle.THIN);
		styleHead.setBorderRight(BorderStyle.THIN);
		styleHead.setBorderTop(BorderStyle.THIN);
		// 生成一个字体
		XSSFFont fontHead = workbook.createFont();
		fontHead.setColor(IndexedColors.BLACK.getIndex());
		fontHead.setFontHeightInPoints((short) 24);
//		fontHead.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontHead.setBold(true);
		// 把字体应用到当前的样式
		styleHead.setFont(fontHead);
		// styleHead
		XSSFRow row0 = sheet.createRow(0);
		XSSFCell createCell = row0.createCell(0);
		row0.setHeightInPoints(30);// 设置行高
		HSSFRichTextString textHead = new HSSFRichTextString("策略开通统计报表");
		createCell.setCellValue(textHead.toString());
		createCell.setCellStyle(styleHead);
		CellRangeAddress region = new CellRangeAddress(0, // first row
				0, // last row
				0, // first column
				headers.length-1 // last column
		);
		sheet.addMergedRegion(region);

		// 产生表格标题行
		XSSFRow row = sheet.createRow(1);
		for (int i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell((short) i);

			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text.toString());
		}
		int bzDescriptionRowIndex = 1;
		// 遍历集合数据，产生数据行
		if (result != null) {
			int index = 2;
			for (List<String> m : result) {
				row = sheet.createRow(index);
				int cellIndex = 0;
				for (String str : m) {
					XSSFCell cell = row.createCell((short) cellIndex);
					cell.setCellValue(str);
					cellIndex++;
				}
				index++;
			}
			bzDescriptionRowIndex = index;
		}

		//空一行出来
		bzDescriptionRowIndex++;

	}

	/**
	 * @Title: exportExcel
	 * @Description: 导出策略生成报表Excel的方法
	 * @param workbook
	 * @param sheetNum
	 *            (sheet的位置，0表示第一个表格中的第一个sheet)
	 * @param sheetTitle
	 *            （sheet的名称）
	 * @param headers
	 *            （表格的标题）
	 * @param result
	 *            （表格的数据）
	 * @param out
	 *            （输出流）
	 * @throws Exception
	 */
	public void exportPolicyGenerateAddData(XSSFWorkbook workbook, int sheetNum, String sheetTitle, String[] headers,
											List<List<String>> result, OutputStream out) throws Exception {
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet();
		workbook.setSheetName(sheetNum, sheetTitle);
		// 设置表格默认列宽度为10个字节
		sheet.setDefaultColumnWidth((short) 10);
		// 生成一个样式
		XSSFCellStyle style = workbook.createCellStyle();
		//设置单元格颜色
		//style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// 设置这些样式
		XSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);// 设置字体大小
		sheet.setColumnWidth(0, 1766);
		sheet.setColumnWidth(1, 7000);
		sheet.setColumnWidth(2, 13000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 7000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 4000);
		sheet.setColumnWidth(9, 4000);
		sheet.setColumnWidth(10, 4000);
		sheet.setColumnWidth(11, 4000);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(13, 4000);
		sheet.setColumnWidth(14, 4000);
		sheet.setColumnWidth(15, 4000);
		sheet.setColumnWidth(16, 4000);
		sheet.setColumnWidth(17, 4000);
		sheet.setColumnWidth(18, 4000);
		sheet.setColumnWidth(19, 4000);
		sheet.setColumnWidth(20, 4000);
		sheet.setColumnWidth(21, 4000);
		sheet.setColumnWidth(22, 4000);
		sheet.setColumnWidth(23, 4000);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		// 生成一个字体
		XSSFFont font1 = workbook.createFont();
		font1.setColor(IndexedColors.BLACK.getIndex());
		font1.setFontHeightInPoints((short) 12);
		font1.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);

		// 产生抬头行
		// 给抬头行生成一个样式
		XSSFCellStyle styleHead = workbook.createCellStyle();
		styleHead.setAlignment(HorizontalAlignment.CENTER);
		styleHead.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		styleHead.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHead.setBorderBottom(BorderStyle.THIN);
		styleHead.setBorderLeft(BorderStyle.THIN);
		styleHead.setBorderRight(BorderStyle.THIN);
		styleHead.setBorderTop(BorderStyle.THIN);
		// 生成一个字体
		XSSFFont fontHead = workbook.createFont();
		fontHead.setColor(IndexedColors.BLACK.getIndex());
		fontHead.setFontHeightInPoints((short) 24);
//		fontHead.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontHead.setBold(true);
		// 把字体应用到当前的样式
		styleHead.setFont(fontHead);
		// styleHead
		XSSFRow row0 = sheet.createRow(0);
		XSSFCell createCell = row0.createCell(0);
		row0.setHeightInPoints(30);// 设置行高
		HSSFRichTextString textHead = new HSSFRichTextString("策略生成统计报表");
		createCell.setCellValue(textHead.toString());
		createCell.setCellStyle(styleHead);
		CellRangeAddress region = new CellRangeAddress(0, // first row
				0, // last row
				0, // first column
				headers.length-1 // last column
		);
		sheet.addMergedRegion(region);

		// 产生表格标题行
		XSSFRow row = sheet.createRow(1);
		for (int i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell((short) i);

			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text.toString());
		}
		int bzDescriptionRowIndex = 1;
		// 遍历集合数据，产生数据行
		if (result != null) {
			int index = 2;
			for (List<String> m : result) {
				row = sheet.createRow(index);
				int cellIndex = 0;
				for (String str : m) {
					XSSFCell cell = row.createCell((short) cellIndex);
					cell.setCellValue(str);
					cellIndex++;
				}
				index++;
			}
			bzDescriptionRowIndex = index;
		}

		//空一行出来
		bzDescriptionRowIndex++;

	}


	/**
	 * @Title: exportExcel
	 * @Description: 导出公用Excel的方法，格式通用
	 * @param workbook
	 * @param sheetNum
	 *            (sheet的位置，0表示第一个表格中的第一个sheet)
	 * @param sheetTitle
	 *            （sheet的名称）
	 * @param headers
	 *            （表格的标题）
	 * @param result
	 *            （表格的数据）
	 * @param out
	 *            （输出流）
	 * @throws Exception
	 */
	public void exportCommonData(XSSFWorkbook workbook, int sheetNum, String sheetTitle, String[] headers,
							   List<List<String>> result, OutputStream out) throws Exception {
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet();
		workbook.setSheetName(sheetNum, sheetTitle);
		// 设置表格默认列宽度为10个字节
		sheet.setDefaultColumnWidth((short) 10);
		// 生成一个样式
		XSSFCellStyle style = workbook.createCellStyle();
		//设置单元格颜色
		//style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// 设置这些样式
		XSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);// 设置字体大小
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);

		// 生成一个字体
		XSSFFont font1 = workbook.createFont();
		font1.setColor(IndexedColors.BLACK.getIndex());
		font1.setFontHeightInPoints((short) 12);
		font1.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);

		// 产生抬头行
		// 给抬头行生成一个样式
		XSSFCellStyle styleHead = workbook.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		styleHead.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHead.setBorderBottom(BorderStyle.THIN);
		styleHead.setBorderLeft(BorderStyle.THIN);
		styleHead.setBorderRight(BorderStyle.THIN);
		styleHead.setBorderTop(BorderStyle.THIN);
		styleHead.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		XSSFFont fontHead = workbook.createFont();
		fontHead.setColor(IndexedColors.BLACK.index);
		fontHead.setFontHeightInPoints((short) 24);
		fontHead.setBold(true);
		// 把字体应用到当前的样式
		styleHead.setFont(fontHead);
		// styleHead
//		XSSFRow row0 = sheet.createRow(0);
//		XSSFCell createCell = row0.createCell(0);
//		row0.setHeightInPoints(30);// 设置行高
//		HSSFRichTextString textHead = new HSSFRichTextString("节点配置统计报表");
//		createCell.setCellValue(textHead.toString());
//		createCell.setCellStyle(styleHead);
//		CellRangeAddress region = new CellRangeAddress(0, // first row
//				0, // last row
//				0, // first column
//				headers.length-1 // last column
//		);
//		sheet.addMergedRegion(region);

		// 产生表格标题行
		XSSFRow row = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell((short) i);

			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text.toString());
		}
		int bzDescriptionRowIndex = 1;
		// 遍历集合数据，产生数据行
		if (result != null) {
			int index = 1;
			for (List<String> m : result) {
				row = sheet.createRow(index);
				int cellIndex = 0;
				for (String str : m) {
					XSSFCell cell = row.createCell((short) cellIndex);
					cell.setCellValue(str);
					cellIndex++;
				}
				index++;
			}
			bzDescriptionRowIndex = index;
		}

		//空一行出来
		bzDescriptionRowIndex++;

	}

	/**
	 * @Title: exportExcel
	 * @Description: 导出策略生成报表Excel的方法
	 * @param workbook
	 * @param sheetNum
	 *            (sheet的位置，0表示第一个表格中的第一个sheet)
	 * @param sheetTitle
	 *            （sheet的名称）
	 * @param headers
	 *            （表格的标题）
	 * @param result
	 *            （表格的数据）
	 * @param out
	 *            （输出流）
	 * @throws Exception
	 *
	 * @param firstRowTitle 表格第一行标题
	 */
	public static void exportPushAutoEmailAddData(XSSFWorkbook workbook, int sheetNum, String sheetTitle, String[] headers,
												  List<List<String>> result, OutputStream out, String firstRowTitle) throws Exception {
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet();
		workbook.setSheetName(sheetNum, sheetTitle);
		// 设置表格默认列宽度为10个字节
		sheet.setDefaultColumnWidth((short) 10);
		// 生成一个样式
		XSSFCellStyle style = workbook.createCellStyle();
		//设置单元格颜色
		//style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// 设置这些样式
		XSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);// 设置字体大小
		sheet.setColumnWidth(0, 1766);
		sheet.setColumnWidth(1, 7000);
		sheet.setColumnWidth(2, 13000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 7000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 4000);
		sheet.setColumnWidth(9, 4000);
		sheet.setColumnWidth(10, 4000);
		sheet.setColumnWidth(11, 4000);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(13, 4000);
		sheet.setColumnWidth(14, 4000);
		sheet.setColumnWidth(15, 4000);
		sheet.setColumnWidth(16, 4000);
		sheet.setColumnWidth(17, 4000);
		sheet.setColumnWidth(18, 4000);
		sheet.setColumnWidth(19, 4000);
		sheet.setColumnWidth(20, 4000);
		sheet.setColumnWidth(21, 4000);
		sheet.setColumnWidth(22, 4000);
		sheet.setColumnWidth(23, 4000);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		// 生成一个字体
		XSSFFont font1 = workbook.createFont();
		font1.setColor(IndexedColors.BLACK.getIndex());
		font1.setFontHeightInPoints((short) 12);
		font1.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);

		// 产生抬头行
		// 给抬头行生成一个样式
		XSSFCellStyle styleHead = workbook.createCellStyle();
		styleHead.setAlignment(HorizontalAlignment.CENTER);
		styleHead.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		styleHead.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHead.setBorderBottom(BorderStyle.THIN);
		styleHead.setBorderLeft(BorderStyle.THIN);
		styleHead.setBorderRight(BorderStyle.THIN);
		styleHead.setBorderTop(BorderStyle.THIN);
		// 生成一个字体
		XSSFFont fontHead = workbook.createFont();
		fontHead.setColor(IndexedColors.BLACK.getIndex());
		fontHead.setFontHeightInPoints((short) 24);
//		fontHead.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontHead.setBold(true);
		// 把字体应用到当前的样式
		styleHead.setFont(fontHead);
		// styleHead
		XSSFRow row0 = sheet.createRow(0);
		XSSFCell createCell = row0.createCell(0);
		row0.setHeightInPoints(30);// 设置行高
		HSSFRichTextString textHead = new HSSFRichTextString(firstRowTitle);
		createCell.setCellValue(textHead.toString());
		createCell.setCellStyle(styleHead);
		CellRangeAddress region = new CellRangeAddress(0, // first row
				0, // last row
				0, // first column
				headers.length-1 // last column
		);
		sheet.addMergedRegion(region);

		// 产生表格标题行
		XSSFRow row = sheet.createRow(1);
		for (int i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell((short) i);

			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text.toString());
		}
		int bzDescriptionRowIndex = 1;
		// 遍历集合数据，产生数据行
		if (result != null) {
			int index = 2;
			for (List<String> m : result) {
				row = sheet.createRow(index);
				int cellIndex = 0;
				for (String str : m) {
					XSSFCell cell = row.createCell((short) cellIndex);
					cell.setCellValue(str);
					cellIndex++;
				}
				index++;
			}
			bzDescriptionRowIndex = index;
		}

		//空一行出来
		bzDescriptionRowIndex++;

	}

	/**
	 * @Title: exportExcel
	 * @Description: 导出公用Excel的方法，格式通用
	 * @param workbook
	 * @param sheetNum
	 *            (sheet的位置，0表示第一个表格中的第一个sheet)
	 * @param sheetTitle
	 *            （sheet的名称）
	 * @param headers
	 *            （表格的标题）
	 * @param result
	 *            （表格的数据）
	 * @param out
	 *            （输出流）
	 * @throws Exception
	 */
	public void exportAutoPushCommonData(XSSFWorkbook workbook, int sheetNum, String sheetTitle, String[] headers,
										 List<List<String>> result, OutputStream out) throws Exception {
		// 生成一个表格
		XSSFSheet sheet = workbook.createSheet();
		workbook.setSheetName(sheetNum, sheetTitle);
		// 设置表格默认列宽度为10个字节
		sheet.setDefaultColumnWidth((short) 10);
		// 生成一个样式
		XSSFCellStyle style = workbook.createCellStyle();
		//设置单元格颜色
		//style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// 设置这些样式
		XSSFFont font = workbook.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);// 设置字体大小目的IP均未找到防护网段配
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 5000);
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 5000);
		sheet.setColumnWidth(13, 5000);
		sheet.setColumnWidth(14, 5000);
		style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);

		// 生成一个字体
		XSSFFont font1 = workbook.createFont();
		font1.setColor(IndexedColors.BLACK.getIndex());
		font1.setFontHeightInPoints((short) 12);
		font1.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 指定当单元格内容显示不下时自动换行
		style.setWrapText(true);

		// 产生抬头行
		// 给抬头行生成一个样式
		XSSFCellStyle styleHead = workbook.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
		styleHead.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHead.setBorderBottom(BorderStyle.THIN);
		styleHead.setBorderLeft(BorderStyle.THIN);
		styleHead.setBorderRight(BorderStyle.THIN);
		styleHead.setBorderTop(BorderStyle.THIN);
		styleHead.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		XSSFFont fontHead = workbook.createFont();
		fontHead.setColor(IndexedColors.BLACK.index);
		fontHead.setFontHeightInPoints((short) 24);
		fontHead.setBold(true);
		// 把字体应用到当前的样式
		styleHead.setFont(fontHead);

		// 产生表格标题行
		XSSFRow row = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			XSSFCell cell = row.createCell((short) i);

			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(headers[i]);
			cell.setCellValue(text.toString());
		}
		int bzDescriptionRowIndex = 1;
		// 遍历集合数据，产生数据行
		if (result != null) {
			int index = 1;
			for (List<String> m : result) {
				row = sheet.createRow(index);
				int cellIndex = 0;
				for (String str : m) {
					XSSFCell cell = row.createCell((short) cellIndex);
					cell.setCellValue(str);
					cellIndex++;
				}
				index++;
			}
			bzDescriptionRowIndex = index;
		}

		//空一行出来
		bzDescriptionRowIndex++;

	}

}
