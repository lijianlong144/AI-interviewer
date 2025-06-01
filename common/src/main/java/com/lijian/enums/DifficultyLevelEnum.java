package com.lijian.enums;

/**
 * 难度等级枚举
 *
 * @author lijian
 * @since 1.0.0
 */
public enum DifficultyLevelEnum {

    /**
     * 入门级
     */
    BEGINNER(1, "入门级"),

    /**
     * 初级
     */
    JUNIOR(2, "初级"),

    /**
     * 中级
     */
    INTERMEDIATE(3, "中级"),

    /**
     * 高级
     */
    SENIOR(4, "高级"),

    /**
     * 专家级
     */
    EXPERT(5, "专家级");

    private final Integer level;
    private final String desc;

    DifficultyLevelEnum(Integer level, String desc) {
        this.level = level;
        this.desc = desc;
    }

    public Integer getLevel() {
        return level;
    }

    public String getDesc() {
        return desc;
    }

    public static DifficultyLevelEnum getByLevel(Integer level) {
        for (DifficultyLevelEnum difficultyLevel : values()) {
            if (difficultyLevel.getLevel().equals(level)) {
                return difficultyLevel;
            }
        }
        return INTERMEDIATE; // 默认返回中级
    }
}
