package com.lijian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lijian.entity.QuestionTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目标签关联Mapper接口
 */
@Mapper
public interface QuestionTagRelationMapper extends BaseMapper<QuestionTagRelation> {
} 