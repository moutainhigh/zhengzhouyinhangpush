package com.abtnetworks.totems.disposal;

/**
 * 全局配置类
 * @author hw
 * @version
 */
public class CommonConfigParam {

	/**
	 * kafka 派发下级协作单位 topic
	 */
	public static final String STR_TOPIC_ASSIGN_BRANCH = "push-disposal-assign-branch";

	/**
	 * kafka 协作处置状态回调 topic
	 */
	public static final String STR_TOPIC_HANDLE_CALLBACK = "push-disposal-handle-callback";

	/**
	 * 显示/隐藏
	 */
	public static final String SHOW = "1";
	public static final String HIDE = "0";

	/**
	 * 是/否
	 */
	public static final String YES = "1";
	public static final String NO = "0";
	
	/**
	 * 对/错
	 */
	public static final String TRUE = "true";
	public static final String FALSE = "false";
}
