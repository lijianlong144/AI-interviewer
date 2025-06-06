package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Position;
import com.lijian.mapper.PositionMapper;
import com.lijian.service.PositionService;
import com.lijian.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 职位管理服务实现类
 */
@Service
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {

    @Override
    @Transactional
    public Position createPosition(Position position) {
        // 生成职位编号
        position.setPositionCode(generatePositionCode());
        
        // 设置默认值
        position.setStatus(1); // 默认为招聘中
        position.setPublishTime(LocalDateTime.now());
        position.setIsDeleted(0);
        
        // 设置创建者
        Long currentUserId = SecurityUtils.getCurrentUserId();
        position.setCreatorId(currentUserId);
        
        // 保存职位
        save(position);
        return position;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Position position = getById(id);
        if (position == null) {
            return false;
        }
        
        position.setStatus(status);
        
        // 如果是关闭职位，设置关闭时间
        if (status == 2) {
            position.setCloseTime(LocalDateTime.now());
        }
        
        return updateById(position);
    }

    @Override
    public Page<Position> pagePositions(Page<Position> page, String title, String department, Integer status) {
        LambdaQueryWrapper<Position> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的
        queryWrapper.eq(Position::getIsDeleted, 0);
        
        // 条件查询
        if (StringUtils.hasText(title)) {
            queryWrapper.like(Position::getTitle, title);
        }
        
        if (StringUtils.hasText(department)) {
            queryWrapper.eq(Position::getDepartment, department);
        }
        
        if (status != null) {
            queryWrapper.eq(Position::getStatus, status);
        }
        
        // 按优先级和发布时间排序
        queryWrapper.orderByDesc(Position::getPriority, Position::getPublishTime);
        
        return page(page, queryWrapper);
    }

    @Override
    public List<Position> getHotPositions(Integer limit) {
        LambdaQueryWrapper<Position> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Position::getStatus, 1) // 招聘中
                .eq(Position::getIsDeleted, 0)
                .orderByDesc(Position::getPriority)
                .last("LIMIT " + limit);
        
        return list(queryWrapper);
    }

    @Override
    public List<Position> getLatestPositions(Integer limit) {
        LambdaQueryWrapper<Position> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Position::getStatus, 1) // 招聘中
                .eq(Position::getIsDeleted, 0)
                .orderByDesc(Position::getPublishTime)
                .last("LIMIT " + limit);
        
        return list(queryWrapper);
    }

    @Override
    public List<Position> getByDepartment(String department) {
        LambdaQueryWrapper<Position> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Position::getDepartment, department)
                .eq(Position::getStatus, 1) // 招聘中
                .eq(Position::getIsDeleted, 0)
                .orderByDesc(Position::getPriority, Position::getPublishTime);
        
        return list(queryWrapper);
    }

    @Override
    public String generatePositionCode() {
        // 生成格式：POS + 年月日 + 4位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 生成4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);
        
        return "POS" + dateStr + randomStr;
    }
}