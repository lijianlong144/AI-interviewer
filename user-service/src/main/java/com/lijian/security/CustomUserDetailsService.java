package com.lijian.security;

import com.lijian.entity.SysUser;
import com.lijian.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义UserDetailsService实现
 *
 * @author lijian
 * @since 1.0.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.getByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return new CustomUserDetails(user);
    }
}