package com.abtnetworks.totems.common.commandline2.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zc
 * @date 2020/01/15
 */
@Slf4j
public class CustomStringUtils {

    private static final String PLUS_MARK = "+";
    private static final String STAR_MARK = "*";
    private static final String QUESTION_MARK = "?";

    /**
     * (xxxxxx)
     */
    static Pattern pattern1 = Pattern.compile("\\([^)]*\\)");

    /**
     * (xxxxxx)*
     */
    static Pattern pattern2 = Pattern.compile("\\([^)]*\\)\\*");

    /**
     * (xxxxxx)+
     */
    static Pattern pattern3 = Pattern.compile("\\([^)]*\\)\\+");

    /**
     * (xxxxxx)?
     */
    static Pattern pattern4 = Pattern.compile("\\([^)]*\\)\\?");

    /**
     * [xxxxxx]
     */
    static Pattern pattern5 = Pattern.compile("\\[[^]]*]");


    /**
     * 关键字替换，左匹配
     * @param text 被处理的文本
     * @param keyWord 关键字
     * @param filler 需要填充关键字的文本
     * @return
     */
    public static String keyWordConsumer(String text, String keyWord, String filler) {
        if (StringUtils.isEmpty(filler)) {
            throw new IllegalArgumentException("数据校验不通过");
        }
        KeyWordFrequency frequency = keyWordDetect(text, keyWord);
        switch (frequency) {
            case NONE:
                throw new IllegalArgumentException("数据校验不通过");
            case ONCE:
                log.debug("未在圆括号中匹配上");
                text = squareBracketsHandle(text, keyWord);
                return StringUtils.replaceOnce(text, keyWord, filler);
            case ANY_TIMES:
            case AT_LEAST_ONCE:
            case AT_MOST_ONCE:
                Matcher matcher1 = pattern1.matcher(text);
                String matchText1 = "";
                while (matcher1.find()) {
                    if (matcher1.group().contains(keyWord)) {
                        matchText1 = matcher1.group();
                        break;
                    }
                }
                String subMatchText = matchText1.substring(1,matchText1.length() -1);
                subMatchText = squareBracketsHandle(subMatchText, keyWord);
                String replacedText = StringUtils.replaceOnce(subMatchText, keyWord, filler);
                switch (frequency) {
                    case AT_LEAST_ONCE:
                        log.debug("(xxxx)+  --->  xxxx(xxxx)*");
                        return StringUtils.replaceOnce(text,matchText1 + PLUS_MARK, replacedText + matchText1 + STAR_MARK);
                    case ANY_TIMES:
                        log.debug("(xxxx)* ---->  xxxx(xxxx)*");
                        return StringUtils.replaceOnce(text, matchText1, replacedText + matchText1);
                    case AT_MOST_ONCE:
                        log.debug("至多一个，覆盖 (xxxxx)?");
                        return StringUtils.replaceOnce(text,matchText1 + QUESTION_MARK, replacedText);
                    default:
                        throw new IllegalArgumentException("未知类型");
                }
                default:
                    throw new IllegalArgumentException("未知类型");
        }
    }

    /**
     * 关键字探测出现的频率
     * @param text
     * @param keyWord
     * @return
     */
    public static KeyWordFrequency keyWordDetect(String text, String keyWord) {
        if (StringUtils.containsNone(text, keyWord)) {
            return KeyWordFrequency.NONE;
        }
        int startKeyWord = text.indexOf(keyWord);

        boolean matchInCircleBrackets = false;
        Matcher matcher1 = pattern1.matcher(text);
        int start1 = -1;
        int end1 = -1;
        while (matcher1.find()) {
            log.debug("存在圆括号");
            if (matcher1.group().contains(keyWord)) {
                log.debug("在圆括号中匹配关键字");
                matchInCircleBrackets = true;
                start1 = matcher1.start();
                end1 = matcher1.end();
                log.debug("匹配到圆括号里的第一个即退出");
                break;
            }
        }

        if (matchInCircleBrackets && startKeyWord > start1) {
            String afterCircleBrackets = String.valueOf(text.charAt(end1));
            switch (afterCircleBrackets) {
                case PLUS_MARK:
                    return KeyWordFrequency.AT_LEAST_ONCE;
                case STAR_MARK:
                    return KeyWordFrequency.ANY_TIMES;
                case QUESTION_MARK:
                    return KeyWordFrequency.AT_MOST_ONCE;
                default:
                    throw new IllegalArgumentException("括号后面必须带上 * ? +");
            }
        } else {
            return KeyWordFrequency.ONCE;
        }
    }

    /**
     * 文中存在 [xx|xx] 找出关键字存在的区间，并用区间替换掉方括号
     * @param text
     * @param keyWord
     * @return
     */
    public static String squareBracketsHandle(String text, String keyWord) {
        if (text.contains(keyWord)) {
            Matcher matcher5 = pattern5.matcher(text);
            while (matcher5.find()) {
                log.debug("存在方括号");
                if (matcher5.group().contains(keyWord)) {
                    log.debug("在方括号中匹配关键字");
                    String matchText5 = matcher5.group();
                    String result = "";
                    String[] strings = matchText5.substring(1, matchText5.length()-1).split("\\|");
                    for (String s : strings) {
                        if (s.contains(keyWord)) {
                            result = s;
                            break;
                        }
                    }
                    text = StringUtils.replaceOnce(text, matchText5, result);
                    break;
                }
            }
        }
        return text;
    }


    /**
     * 关键字替换
     * @param text 被处理的文本
     * @param keyWord 关键字
     * @param fillerList 需要填充关键字的文本集合， 注意：该集合大小会有变化，被消费掉的数据会被移除
     * @return
     */
    public static String keyWordConsumer(String text, String keyWord, ArrayList<String> fillerList) {
        if (StringUtils.containsNone(text, keyWord) || CollectionUtils.isEmpty(fillerList)) {
            throw new IllegalArgumentException("数据校验不通过");
        }
        while (fillerList.size() > 0 && text.contains(keyWord)) {
            text = keyWordConsumer(text, keyWord, fillerList.get(0));
            fillerList.remove(0);
        }
        return text;
    }

    /**
     * 替换两个关键字
     * @param text
     * @param keyWord1
     * @param filler1
     * @param keyWord2
     * @param filler2
     * @return
     */
    public static String biKeyWordConsumer(String text, String keyWord1, String filler1, String keyWord2, String filler2) {
        if (StringUtils.isAnyEmpty(filler1,filler2) || StringUtils.containsNone(text, keyWord1) || StringUtils.containsNone(text, keyWord2)) {
            throw new IllegalArgumentException("数据校验不通过");
        }
        text = keyWordConsumer(text, keyWord1, filler1);
        return keyWordConsumer(text, keyWord2, filler2);
    }



    /**
     * 清除掉文本中 (xx)* 和 (xx)? 这样的文本
     * @param text
     * @return
     */
    public static String clearSpecialText(String text) {
        Matcher matcher2 = pattern2.matcher(text);
        while (matcher2.find()) {
            String matcherGroup = matcher2.group();
            text = text.replace(matcherGroup, "");
        }
        Matcher matcher4 = pattern4.matcher(text);
        while (matcher4.find()) {
            String matcherGroup = matcher4.group();
            text = text.replace(matcherGroup, "");
        }
        return text;
    }

    public enum KeyWordFrequency {
        /**
         * 零次
         */
        NONE,
        /**
         * 一次
         */
        ONCE,
        /**
         * 至多一次
         */
        AT_MOST_ONCE,
        /**
         * 至少一次
         */
        AT_LEAST_ONCE,
        /**
         * 任意次
         */
        ANY_TIMES
    }

    public static void main(String[] args) {

        String msg = "ip address-set (<address-group-name>)* (type)* group address <index> [(<address-name>)*| address-set] <address-name>\nquit";
        String keyWord = "<address-name>";
        ArrayList<String> list = new ArrayList<>();
        list.add("111");
        list.add("222");
//        String result = keyWordConsumer(msg, keyWord, list);
//        String result = clearSpecialText(msg);
        String result = squareBracketsHandle(msg, keyWord);
        System.out.println(result);
//        System.out.println(list.size());
    }

}
