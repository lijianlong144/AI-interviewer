// common/src/main/java/com/lijian/common/enums/UserRole.java
package com.lijian.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ADMIN", "管理员"),
    HR("HR", "HR/面试官"),
    CANDIDATE("CANDIDATE", "候选人");

    private final String code;
    private final String desc;

    UserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}