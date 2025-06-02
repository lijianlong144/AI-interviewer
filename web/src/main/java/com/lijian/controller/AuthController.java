package com.lijian.controller;

import com.lijian.dto.LoginDTO;
import com.lijian.dto.RegisterDTO;
import com.lijian.entity.SysUser;
import com.lijian.result.Result;
import com.lijian.security.CustomUserDetails;
import com.lijian.service.SysUserService;
import com.lijian.utils.JwtUtil;
import com.lijian.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // 创建认证对象
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        // 执行认证
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 设置认证信息到Security上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SysUser user = userDetails.getUser();

        // 生成JWT
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setTokenType("Bearer");
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());
        loginVO.setEmail(user.getEmail());
        loginVO.setPhone(user.getPhone());
        loginVO.setAvatar(user.getAvatar());

        return Result.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        // 创建用户对象
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setRealName(registerDTO.getRealName());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "CANDIDATE");

        // 注册用户
        SysUser registeredUser = userService.register(user, null);

        // 自动登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword(registerDTO.getPassword());

        return login(loginDTO);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7);
        String newToken = jwtUtil.refreshToken(token);

        // 获取用户信息
        String username = jwtUtil.getUsernameFromToken(newToken);
        SysUser user = userService.getByUsername(username);

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(newToken);
        loginVO.setTokenType("Bearer");
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());
        loginVO.setEmail(user.getEmail());
        loginVO.setPhone(user.getPhone());
        loginVO.setAvatar(user.getAvatar());

        return Result.success(loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<SysUser> getCurrentUser(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SysUser user = userService.getUserDetail(userDetails.getUserId());
        return Result.success(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.success();
    }
}