package com.abtnetworks.totems.common.config;

/**
 * 全局配置类
 * @author hw
 * @version
 */
public class Global {

	/**
	 * 当前对象实例
	 */
	private static Global global = new Global();

	/**
	 * 获取当前对象实例
	 */
	public static Global getInstance() {
		return global;
	}

	/**
	 * 攻击面进度
	 */
	public static String DANGER_PERCENTAGE = "0";

	/**
	 * 风险规则总进度
	 */
	public static String RISK_RULE_PERCENTAGE = "0";

	/**
	 * 域间风险进度
	 */
	public static String RISK_ZONE_ACCESS_PERCENTAGE = "0";

	/**
	 * 策略风险进度
	 */
	public static String RISK_POLICY_PERCENTAGE = "0";

	/**
	 * 域间白名单检查进度
	 */
	public static String ZONE_ACCESS_LIST_PERCENTAGE = "0";

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

	/**
	 * 判断操作系统是Windows或者Linux。
	 */
	public static final Boolean ISWINDOWS = System.getProperties().getProperty("os.name").toLowerCase().contains("windows");

	public static void main(String[] args) {
		System.out.println(ISWINDOWS);
	}
}
