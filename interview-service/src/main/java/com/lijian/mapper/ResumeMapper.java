package com.lijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lijian.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简历Mapper接口
 */
@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}