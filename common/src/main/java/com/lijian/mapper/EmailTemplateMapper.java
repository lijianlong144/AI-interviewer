package com.lijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lijian.entity.EmailTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件模板Mapper接口
 */
@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplate> {
}