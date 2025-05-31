package com.lijian.service;

import com.lijian.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User getUserById(String id) {
        // 这里可以添加逻辑来从数据库或其他数据源获取用户信息
        User user = new User();
        user.setId(id);
        user.setName("测试用户");
        user.setEmail("test@example.com");
        return user;
    }
}
