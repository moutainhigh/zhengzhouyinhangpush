package com.abtnetworks.totems.common.constants;

import java.util.regex.Pattern;

/**
 * @Des 公共的常量
 * @Author: wenjiachang
 * @Date: 2019/3/7 11:41
 *  * @Description: <p> 封堵常量 {@link DisposalConstants }
 *  *  策略仿真常量 {@link PolicyConstants}
 *  *  生成常量 {@link GenerateConstants}
 *  *  高级设置常量 {@link AdvancedSettingsConstants}
 *  *  公共类常量{@link CommonConstants}
 *  *  凭据常量{@link CredentialConstants}
 *  *  下发常量{@link PushConstants}
 */
public class CommonConstants {
    /*****/
    public static final String IPV4 = "IPV4";
    public static final String IPV6 = "IPV6";


    public static final String PUSH_COMMAND_FILE_DOWNLOAD_FOLDER = "push_command_file";


    /**山石型号**/
    public static final String HILLSTONESTONEOS_KEY = "HillstoneStoneOS";


    /**
     * 换行
     */
    public static final String LINE_BREAK = "\n";
    /**
     * any
     *
     */
    public static final String  ANY = "any";
    /**
     * deny
     *
     */
    public static final String  DENY = "deny";
    /**checkpoint中拼的特殊命令**/
    public static final String INSTALL_LAYER="install-on <deviceName>";

    /**h3sec po **/
    public static final String OBJECT_POLICY = "<policySetName>";

    /**h3sec po_pre **/
    public static final String pre_commandLine = "display";

    /**策略ID占位符**/
    public static final String LABEL_POLICY_ID = "<policyID>";

    /**迪普的地址组对象前缀**//**迪普的地址对象前缀匹配**/
    public static final Pattern IP_OBJ_PATTERN = Pattern.compile("([IP_OBJ_|IP_OBJ_GRP_]+)(?<obj1>.*).*?");
    /**默认layer**/
    public static final String DEFAULT_LAYER_NAME = "Network";
    /**小时**/
    public static final Integer HOUR_SECOND = 60 * 60;
    /**小时IdleTimeout**/
    public static final  Pattern IDLE_TIMEOUT_PATTERN = Pattern.compile("[0-9]*");

    /**
     * 占位符
     */
    public static final String POLICY_ID = "<policyId>";
    /**
     * 天融信show前缀
     */
    public static final String POLICY_SHOW_TOP_SEC = "policy show";
    /**
     * 天融信show前缀
     */
    public static final String POLICY_TOP_SEC_MOVE_FLAG = "policy move";
    /**
     * 飞塔show前缀
     */
    public static final String POLICY_SHOW_FORTINET = "show firewall policy";

    /**
     * 思科路由交换设备根据策略集名称show
     */
    public static final String SHOW_POLICY_ACCESSNAME = "show access-lists";

    /**
     * 策略中目的地址是域名对象的标识
     */
    public static final String POLICY_SOURCE_DST_DOMAIN = "1";

    /**
     * 策略生成中的源域
     */
    public static final String SRC_ZONE = "SRC_ZONE";
    /**
     * 策略生成中的目的域
     */
    public static final String DST_ZONE = "DST_ZONE";
    /**
     * 策略生成中的源接口
     */
    public static final String SRC_ITF = "SRC_ITF";
    /**
     * 策略生成中的目的接口
     */
    public static final String DST_ITF = "DST_ITF";
    /**
     * 策略生成中的设备uuid
     */
    public static final String DEVICE_UUID = "DEVICE_UUID";

	/**
     * H3V7 查询域间策略集
     */
    public static final String POLICY_SHOW_ZONE_PAIR = "display packet-filter zone-pair security";

	/**
     * H3V7 策略集占位符
     */
    public static final String POLICY_ACL_ADVANCED_IPV4_NAME = "access-list %s %s";

    public static final String POLICY_ACL_ADVANCED_IPV6_NAME = "access-list ipv6 %s %s";


    public static final String POLICY_NAME_ZONE = "zone-pair security";

    public static final String POLICY_NAME_IP4 = "security-policy ip";

    public static final String POLICY_NAME_IP6 = "security-policy ipv6";
    /**占位符**/
    public static final String PLACE_HOLDER = "#1";

    /**占位符2**/
    public static final String PLACE_HOLDER_2 = "#2";

    /**占位符**/
    public static final String FORTINET_PLACE_HOLDER = "<policyId>";




}
