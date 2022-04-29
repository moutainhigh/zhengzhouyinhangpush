package com.abtnetworks.totems.translation;

import java.io.Serializable;


/**
 * Entity支持类
 * @author
 * @version
 */
public abstract class PaginationParamDto implements Serializable {

	/**
	 * 删除标记（0：正常；1：删除；2：审核；）
	 */
	public static final String DEL_FLAG_NORMAL = "0";
	public static final String DEL_FLAG_DELETE = "1";
	public static final String DEL_FLAG_AUDIT = "2";

	/**
	 * 页数
	 */
	private Integer page = 1;

	/**
	 * 每页条数
	 */
	private Integer limit = 20;

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
}
