package com.lijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lijian.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件日志Mapper接口
 */
@Mapper
public interface EmailLogMapper extends BaseMapper<EmailLog> {
}