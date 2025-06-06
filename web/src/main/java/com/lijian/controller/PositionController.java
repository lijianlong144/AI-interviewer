package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.Position;
import com.lijian.result.Result;
import com.lijian.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/position")
@Validated
public class PositionController {

    @Autowired
    private PositionService positionService;

    /**
     * 创建职位
     */
    @PostMapping
    public Result<Position> createPosition(@Valid @RequestBody Position position) {
        Position createdPosition = positionService.createPosition(position);
        return Result.success(createdPosition);
    }

    /**
     * 更新职位
     */
    @PutMapping
    public Result<Boolean> updatePosition(@Valid @RequestBody Position position) {
        boolean result = positionService.updateById(position);
        return Result.success(result);
    }

    /**
     * 获取职位详情
     */
    @GetMapping("/{id}")
    public Result<Position> getPositionDetail(@PathVariable("id") Long id) {
        Position position = positionService.getById(id);
        return Result.success(position);
    }

    /**
     * 更新职位状态
     */
    @PutMapping("/status/{id}")
    public Result<Boolean> updateStatus(@PathVariable("id") Long id, @RequestParam(value = "status") Integer status) {
        boolean result = positionService.updateStatus(id, status);
        return Result.success(result);
    }

    /**
     * 删除职位
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deletePosition(@PathVariable("id") Long id) {
        // 逻辑删除
        Position position = new Position();
        position.setId(id);
        position.setIsDeleted(1);
        boolean result = positionService.updateById(position);
        return Result.success(result);
    }

    /**
     * 分页查询职位
     */
    @GetMapping("/page")
    public Result<Page<Position>> pagePositions(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                               @RequestParam(value = "size", defaultValue = "10") Integer size,
                                               @RequestParam(value = "title", required = false) String title,
                                               @RequestParam(value = "department", required = false) String department,
                                               @RequestParam(value = "status", required = false) Integer status) {
        Page<Position> page = new Page<>(current, size);
        Page<Position> resultPage = positionService.pagePositions(page, title, department, status);
        return Result.success(resultPage);
    }

    /**
     * 获取热门职位
     */
    @GetMapping("/hot")
    public Result<List<Position>> getHotPositions(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        List<Position> positions = positionService.getHotPositions(limit);
        return Result.success(positions);
    }

    /**
     * 获取最新职位
     */
    @GetMapping("/latest")
    public Result<List<Position>> getLatestPositions(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        List<Position> positions = positionService.getLatestPositions(limit);
        return Result.success(positions);
    }

    /**
     * 根据部门获取职位
     */
    @GetMapping("/department")
    public Result<List<Position>> getByDepartment( @RequestParam(value = "department") String department) {
        List<Position> positions = positionService.getByDepartment(department);
        return Result.success(positions);
    }
}