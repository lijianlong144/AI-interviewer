package com.lijian;


import com.lijian.entity.SysUser;
import com.lijian.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestService {

    @Autowired
    SysUserService userService;

    @Test
    public void register () {



        SysUser user = new SysUser();
        user.setUsername("lijianlong1314");
        user.setPassword("testPassword");
        user.setEmail("lijianlong1314@qq.com");

        SysUser register = userService.register(user, null);
        System.out.printf("注册用户: %s, ID: %d%n", register.getUsername(), register.getId());
    }

    @Test
    public void getByUsername(){

        SysUser user = userService.getByUsername("lijian");
        if (user != null) {
            System.out.printf("用户信息: %s, ID: %d%n", user.getUsername(), user.getId());
        } else {
            System.out.println("用户不存在");
        }



    }

}
