package com.lijian.enums;

/**
 * 推荐决策枚举
 *
 * @author lijian
 * @since 1.0.0
 */
public enum RecommendationEnum {

    /**
     * 强烈推荐
     */
    STRONGLY_RECOMMEND("STRONGLY_RECOMMEND", "强烈推荐"),

    /**
     * 推荐
     */
    RECOMMEND("RECOMMEND", "推荐"),

    /**
     * 中性
     */
    NEUTRAL("NEUTRAL", "中性"),

    /**
     * 不推荐
     */
    NOT_RECOMMEND("NOT_RECOMMEND", "不推荐");

    private final String code;
    private final String desc;

    RecommendationEnum(String code, String desc) {
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