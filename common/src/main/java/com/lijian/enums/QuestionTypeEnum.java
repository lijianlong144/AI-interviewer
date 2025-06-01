package com.lijian.enums;

/**
 * 问题类型枚举
 *
 * @author lijian
 * @since 1.0.0
 */
public enum QuestionTypeEnum {

    /**
     * 技术题
     */
    TECHNICAL("TECHNICAL", "技术题"),

    /**
     * 行为题
     */
    BEHAVIORAL("BEHAVIORAL", "行为题"),

    /**
     * 情景题
     */
    SITUATIONAL("SITUATIONAL", "情景题"),

    /**
     * AI生成题
     */
    AI("AI", "AI生成题"),

    /**
     * 题库题
     */
    BANK("BANK", "题库题");

    private final String code;
    private final String desc;

    QuestionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}