package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.Position;

import java.util.List;

/**
 * 职位管理服务接口
 */
public interface PositionService extends IService<Position> {

    /**
     * 创建职位
     * @param position 职位信息
     * @return 创建后的职位
     */
    Position createPosition(Position position);

    /**
     * 更新职位状态
     * @param id 职位ID
     * @param status 状态：1-招聘中 2-已关闭 3-已暂停
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 分页查询职位列表
     * @param page 分页参数
     * @param title 职位名称
     * @param department 部门
     * @param status 状态
     * @return 职位分页列表
     */
    Page<Position> pagePositions(Page<Position> page, String title, String department, Integer status);

    /**
     * 获取热门职位
     * @param limit 数量限制
     * @return 热门职位列表
     */
    List<Position> getHotPositions(Integer limit);

    /**
     * 获取最新职位
     * @param limit 数量限制
     * @return 最新职位列表
     */
    List<Position> getLatestPositions(Integer limit);

    /**
     * 根据部门获取职位
     * @param department 部门
     * @return 职位列表
     */
    List<Position> getByDepartment(String department);

    /**
     * 生成职位编号
     * @return 职位编号
     */
    String generatePositionCode();
}