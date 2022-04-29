package com.abtnetworks.totems.issued.dto;

import java.io.Serializable;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 请写类注释
 */
public class CommandRegularParamPageDTO implements Serializable {

    private static final long serialVersionUID = -3187360517151000667L;
    /***
     * 搜索字段
     */
    private String searchName;
    /***
     * 设备类型
     */
    private String type;
    /***
     * 厂家名称
     */
    private String vendorName;
    /***
     * 修改人
     */
    private String updateEmp;

    /**
     * 页面大小
     **/
    private Integer pageSize;
    /**
     * 当前页
     **/
    private Integer currentPage;

    public String getUpdateEmp() {
        return updateEmp;
    }

    public void setUpdateEmp(String updateEmp) {
        this.updateEmp = updateEmp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
