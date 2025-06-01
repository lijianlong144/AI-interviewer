package com.lijian.enums;

/**
 * 评分等级枚举
 *
 * @author lijian
 * @since 1.0.0
 */
public enum ScoreRangeEnum {

    /**
     * 优秀
     */
    EXCELLENT(90, 100, "优秀"),

    /**
     * 良好
     */
    GOOD(80, 89, "良好"),

    /**
     * 一般
     */
    AVERAGE(70, 79, "一般"),

    /**
     * 较差
     */
    POOR(60, 69, "较差"),

    /**
     * 不合格
     */
    FAIL(0, 59, "不合格");

    private final Integer minScore;
    private final Integer maxScore;
    private final String desc;

    ScoreRangeEnum(Integer minScore, Integer maxScore, String desc) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.desc = desc;
    }

    public Integer getMinScore() {
        return minScore;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public String getDesc() {
        return desc;
    }

    public static ScoreRangeEnum getByScore(Double score) {
        for (ScoreRangeEnum scoreRange : values()) {
            if (score >= scoreRange.getMinScore() && score <= scoreRange.getMaxScore()) {
                return scoreRange;
            }
        }
        return FAIL; // 默认返回不合格
    }
}