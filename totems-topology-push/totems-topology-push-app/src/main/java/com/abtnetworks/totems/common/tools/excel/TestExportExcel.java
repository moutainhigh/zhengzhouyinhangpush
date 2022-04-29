package com.abtnetworks.totems.common.tools.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestExportExcel {

    public static void main(String[] args) throws FileNotFoundException {
        // 准备数据
        List<Student> list = new ArrayList<Student>();
        for (int i = 0; i < 100; i++) {
            list.add(new Student(111,"张三asdf","男"));
            list.add(new Student(111,"李四asd","男"));
            list.add(new Student(111,"王五","女"));
            list.add(new Student(111,"张三asdf","男"));
            list.add(new Student(111,"李四asd","男"));
            list.add(new Student(111,"王五","女"));
            list.add(new Student(111,"张三asdf","男"));
            list.add(new Student(111,"李四asd","男"));
            list.add(new Student(111,"王五","女"));
            list.add(new Student(111,"张三asdf","男"));
        }
        long start = System.currentTimeMillis();
        System.out.println("开始导出：" + start);
        String[] headers = { "姓名", "性别" };
        String[] columnNames = { "name", "sex"};
        ExportExcelUtil.exportExcel("用户导出", headers, columnNames, list, new FileOutputStream("E:/test.xls"));
        long end = System.currentTimeMillis();
        System.out.println("结束导出：" + end);
        System.out.println("耗时：" + (end-start));
    }
}

class Student {
    private int id;
    private String name;
    private String sex;

    public Student(int id, String name, String sex) {
        this.id = id;
        this.name = name;
        this.sex = sex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}