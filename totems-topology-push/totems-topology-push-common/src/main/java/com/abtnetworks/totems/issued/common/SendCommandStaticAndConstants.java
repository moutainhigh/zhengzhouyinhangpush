package com.abtnetworks.totems.issued.common;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @Author: Administrator
 * @Date: 2019/12/16
 * @desc: 下发命令中使用的常量
 */
public class SendCommandStaticAndConstants {

    /**
     * 超时标识
     */
    public static final int COMMAND_EXECUTION_SUCCESS_OPCODE = -2;
    /**
     * 回车标识
     */
    public final static String ENTER_CHARACTER = "\r";
    /**
     * 换行
     */
    public static final String LINE_BREAK = "\n";

    /**
     * 逗号分隔
     */
    public static final String COMMA_SPLIT = ",";


    /****?
     * 给思科正则匹配到特殊telnet登陆情况
     */
    public static final String CISCO_REG = "Cisco";


    /**
     * 默认中止符
     **/
    public static final String[] LINUX_PROMPT_REG_EX = {"\\>", "#", "~#"};
    /**
     * 终止标记
     **/
    public static final String DEFAULT_PROMPT = "PROMPT_FLAG";
    /**
     * key
     **/
    public static final String KEY = "key";
    /**
     * value
     **/
    public static final String VALUE = "value";
    /**
     * 连接方式
     **/
    public static final String SSH_TYPE = "ssh";
    public static final String TELNET_TYPE = "telnet";

    /**
     * 密码
     **/
    public static final String PASSWORD = "password";

    public static final String SSH_SESSION = "SSH_SESSION";

    public static final String CACHE_NAME = "cacheIssued";

    /***
     * 命令行回显map存储，作为全局使用
     * 在最后取出这次下发的，需清空key和value
     * key : 这次下发过程中产生的唯一的标识
     * value: 这次下发的回显提出来显示，全程可用每次回显命令行
     */
    public static ConcurrentHashMap<String, List<String>> echoCmdMap = new ConcurrentHashMap<>();

    /***华三v7规则名字命令的点位***/
    public static final String H3V7_NAME_FLAG = "rule name";
    /***飞塔规则名字命令的点位***/
    public static final String FORTINET_NAME_FLAG = "show";
    public static final String FORTINET_MOVE_FLAG = "move";
    public static final String FORTINET_ID_FLAG = "edit";
    /***华三v7查询规则id命令的点位dis security-policy ip | include rule.[0-9]+.name.工单号$ 查询唯一策略名称 ***/
    public static final String H3V7_SHOW_RULE_ID = "dis security-policy ip | include rule.[0-9]+.name.";
    /***华三v7查询规则id命令的点位***/
    public static final String H3V7_SHOW_RULE_ID_IPV6 = "dis security-policy ipv6 | include rule.[0-9]+.name.";
    /***华三v7移动id命令的点位之前 ***/
    public static final String H3V7_MOVE_RULE_ID_BEFORE = "move rule #1 before #2 ";
    /***华三v7移动id命令的点位之后 ***/
    public static final String H3V7_MOVE_RULE_ID_AFTER = "move rule #1 after #2 ";

    /***飞塔移动id命令的点位之前 ***/
    public static final String FORTINET_MOVE_RULE_ID_BEFORE = "move #1 before #2 ";
    /***飞塔移动id命令的点位之后 ***/
    public static final String FORTINET_MOVE_RULE_ID_AFTER = "move #1 after #2 ";
    /**
     * 对华三v7进入可以编辑的移动的命令行
     ***/
    public static final String H3V7_SYSTEM_VIEW = "system-view";
    /**
     * 对华三v7进入可以编辑的移动的命令行
     **/
    public static final String H3V7_SECURITY_POLICY = "security-policy ip";
    public static final String H3V7_SECURITY_IPV6_POLICY = "security-policy ipv6";

    /**
     * 对飞塔进入可以编辑的移动的命令行
     **/
    public static final String FORTINE_SECURITY_POLICY = "config firewall policy";
    public static final String SHOW_FORTINE_SECURITY_POLICY = "show firewall policy";
    /**
     * IPV4 show rule
     **/
    public static final String H3V7_SHOW_RULE = "dis security-policy ip | include rule";
    /**
     * ipv6 show rule
     **/
    public static final String H3V7_SHOW_IPV6_RULE = "dis security-policy ipv6 | include rule";
    /***交互式遇到more**/
    public static final String H3V7_MORE_REG = "---- More ----";

    public static final String HILLESTONE_MORE_REG = "--More--";
    /****/
    public static final String H3V7_RETURN = "return";
    /*****华为u6000*****/
    public static final String HW_U6000_SHOW = "display current-configuration | include rule name ";
    /**
     * U6000第二级目录
     **/
    public static final String HW_U6000_SECURITY_POLICY = "security-policy";
    /***华为移动前命令***/
    public static final String HW_U6000_MOVE_RULE_BEFORE = "rule move #1 before #2";
    /**
     * 华为置顶
     **/
    public static final String HW_U6000_MOVE_TOP = "rule move #1 top";
    /***华为移动后命令***/
    public static final String HW_U6000_MOVE_RULE_AFTER = "rule move #1 after #2";

    /***华为进入虚墙的命令***/
    public static final String HW_U6000_ENTRY_VSY_S = "switch vsys #1 ";

    /**
     * 山石查找名字的id命令
     **/
    public static final String HILLSTONE_SHOW_NAME = " show policy | include ";

    /**山石hillstoneOS正则自己的名字 **/
    public static final Pattern HILLSTONE_SHOW_NAME_OWNER = Pattern.compile("Rule\\sid\\s(?<id>\\d*)\\sis\\screated");

    /**
     * 山石查找名字的替换正则命令
     **/
    public static final Pattern HILL_PATTERN = Pattern.compile("#1");
    /**
     * 华三查找id的替换正则命令
     **/
    public static final Pattern RULE_NAME_RGE = Pattern.compile("rule\\s+(?<obj1>.*?)\\s+name\\s.*?");
    /**
     * 天荣信查找id的替换正则命令
     **/
    public static final Pattern RULE_ACTON_RGE = Pattern.compile("rule\\s+(?<obj1>.*?)\\s+action\\s.*?");
    /**
     * 根据工单的名字，来找到自己名字 rule 11 name 3c12\r\r rule 11 name h3-c164tdntgdfngfh-\r\r
     **/
    public static final Pattern RULE_NAME_OWNER = Pattern.compile("rule\\s(?<id>\\d*)\\sname\\s(?<name>[a-zA-Z0-9-_]+).*?");

    /**
     * 飞塔根据策略名称,获取策略id  edit 2232  匹配2232
     */
    public static final Pattern  FORTINET_RULE_NAME_OWNER = Pattern.compile("edit\\s(?<id>\\d*)");

    /**
     * 获取策略集下面acl命令行的行号  Extended IP access list FOTIC-MaYiJinFu
     *     10  permit ip host 10.7.68.124 host 10.7.8.169
     *     20  permit ip host 10.7.68.125 host 10.7.8.169
     *     30  deny ip host 10.7.68.121 host 10.7.8.161
     *     匹配10,20
     */
    public static final Pattern  ACL_COMMANDLINE_MATCH = Pattern.compile("(?<id>\\d*)\\s+(permit|deny)");
    /**
     * h3c op 的查询命令
     **/
    public static final String H3C_POLICY_SHOW = "display object-policy zone-pair security source #1 destination #2";
    /**
     * h3c op 的命令中找到策略名
     **/
    public static final Pattern H3C_OBJECT_POLICY = Pattern.compile("object-policy\\sapply\\sip\\s+(?<obj1>[\\w-]+).*?");
    /**
     * h3c op 的命令中找到策略id和创建策略的时候的对象名称字符串
     * 格式： rule pass source-ip wewqq_AO_8066 destination-ip wewqq_AO_8502 service tcp_43
     **/
    public static final Pattern H3C_OBJECT_POLICYI_FIND_ID = Pattern.compile("rule\\s+(?<id>\\d*)\\s+(pass|drop)\\s+(?<policyInfo>[\\s\\S]*)");

    /**
     * 华三查找id的替换正则命令
     **/
    public static final Pattern TOP_SEC_010_POLICY_ID_RGE = Pattern.compile("ID\\s+(?<obj1>.*?)\\s+firewall.*?");
    /**
     * 天融信查找id的替换正则命令
     **/
    public static final Pattern TOP_SEC_010_POLICY_ID_RGE_NAT = Pattern.compile("ID\\s+(?<obj1>.*?)\\s+nat.*?");
    /**
     * h3c v5 的命令中找到策略id
     **/
    public static final Pattern H3C_V5_RGE = Pattern.compile("rule\\s(?<id>\\d*)");
    /**
     * h3c v5 的命令中移动策略id
     **/
    public static final String H3C_V5_MOVE = "move rule #1 before #2 ";


    /**
     * 天融信查找名字的替换正则命令
     **/
    public static final String TOP_SEC_SHOW = "firewall policy show";
    /**
     * 天融信查找groupName内第一条
     **/
    public static final String TOP_SEC_SHOW_GROUP = "firewall group_policy show name #1";
    /**
     * 常用占位符
     **/
    public static final String DEFAULT_GROUP_BEFORE = "firewall policy move_by_name #1 before #2";

    /**
     * 天融信 匹配地址对象名称，服务对象名称
     **/
    public static final Pattern TOPSEC_OBJECT_MATCH = Pattern.compile("add action\\s(accept|deny)(?<obj1>[\\s\\S]*).*");

    /**
     * 天融信 匹配地址对象名称，服务对象名称
     **/
    public static final String TOP_SEC_SHOW_V2 = "firewall policy show #1";

    /**
     * 天融信的show more动作
     ***/
    public static final String TOP_SEC_MORE = "--More--";

    /***ctrl c 表示方法**/
    public static final String CTRL_C = "^C\n";

    /***华三v7规则名字命令的点位***/
    public static final String TOP_SEC_NAME_FLAG = "policy add name";

    /**
     * CHECK POINT 对策略包发送到对应目标主机上
     **/
    public static final String CHECK_POINT_PACKAGE_TARGET = "mgmt install-policy policy-package \"#1\" access true threat-prevention false targets.1 \"#2\"\n";

    /**
     * H3V7 域间策略集名
     **/
    public static final Pattern H3CV7_ZONE_PAIR = Pattern.compile("(?<ipType>IPv4|IPv6)\\s+?ACL\\s(?<zonePair>\\w*)\\b");


    public static final Pattern H3CV7_COMMAND_LINE_ZONE_PAIR = Pattern.compile("display\\s+packet-filter\\s+zone-pair\\s+security\\s+source\\s(?<srcZone>\\w+?)\\s+destination\\s+(?<dstZone>\\w+?)\\b");

    /**SSG查找id的替换正则命令**/
    public static final Pattern SSG_POLICY_ID_RGE = Pattern.compile("policy id =\\s+(?<id>\\d*).*?");

}
