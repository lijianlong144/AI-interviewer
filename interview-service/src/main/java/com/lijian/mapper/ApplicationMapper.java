package com.lijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lijian.entity.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职位申请Mapper接口
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
}