package com.abtnetworks.totems.command.line.enums;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/6 18:04'.
 */
public enum SymbolsEnum {

    PERIOD("1", ".", "句号"),
    COMMA("2", ",", "逗号"),
    COLON("3", ":", "冒号"),
    SEMICOLON("4", ";", "分号"),
    EXCLAMATION("5", "!", "惊叹号"),
    QUESTION_MARK("6", "?", "问号"),
    VIRGULE("7", "/", "斜线号"),
    AMPERSAND("8", "&", "and"),
    PLUS("9", "+", "加号"),
    MINUS("10", "-", "减号"),
    UNDERLINE("11", "_", "下划线"),
    OCTOTHORPE("12", "#", "井号"),
    AT("13", "@", "@符号"),
    PARALLEL("14", "||", "双线号")

    ;


    private String number;
    private String value;
    private String description;

    SymbolsEnum(String number, String value, String description) {
        this.number = number;
        this.value = value;
        this.description = description;
    }

    public String getNumber() {
        return number;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
