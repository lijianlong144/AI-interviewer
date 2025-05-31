package com.lijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lijian.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author lijian
 * @since 1.0.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
