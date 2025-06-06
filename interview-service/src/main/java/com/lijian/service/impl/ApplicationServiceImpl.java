package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Application;
import com.lijian.entity.Position;
import com.lijian.entity.Resume;
import com.lijian.mapper.ApplicationMapper;
import com.lijian.service.ApplicationService;
import com.lijian.service.PositionService;
import com.lijian.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 职位申请服务实现类
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private PositionService positionService;

    @Autowired
    private ResumeService resumeService;

    @Override
    @Transactional
    public Application createApplication(Application application) {
        // 检查职位是否存在
        Position position = positionService.getById(application.getPositionId());
        if (position == null || position.getStatus() != 1) {
            throw new RuntimeException("职位不存在或已关闭");
        }

        // 检查简历是否存在
        Resume resume = resumeService.getById(application.getResumeId());
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        // 检查是否已申请过该职位
        boolean hasApplied = checkIfApplied(application.getCandidateId(), application.getPositionId());
        if (hasApplied) {
            throw new RuntimeException("您已申请过该职位");
        }

        // 生成申请编号
        application.setApplicationNo(generateApplicationNo());
        
        // 设置申请信息
        application.setStatus("PENDING");
        application.setSource("WEBSITE"); // 默认来源TODO，可做可不做，只有端
        application.setIsRead(0); // 未读
        application.setApplyTime(LocalDateTime.now());
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        // 保存申请
        save(application);
        return application;
    }

    @Override
    public boolean updateStatus(Long id, String status, String remark) {
        Application application = getById(id);
        if (application == null) {
            return false;
        }

        application.setStatus(status);
        application.setHrComment(remark);
        application.setUpdateTime(LocalDateTime.now());
        application.setIsRead(1);
        application.setReadTime(LocalDateTime.now());
        // 如果是筛选操作，记录筛选时间
        if ("PASSED".equals(status) || "REJECTED".equals(status)) {
            application.setScreenTime(LocalDateTime.now());
            if ("REJECTED".equals(status)) {
                application.setRejectReason(remark);
            }
        }

        return updateById(application);
    }

    @Override
    public Page<Application> pageApplications(Page<Application> page, Long candidateId, Long positionId, String status) {
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();

        // 条件查询
        if (candidateId != null) {
            queryWrapper.eq(Application::getCandidateId, candidateId);
        }

        if (positionId != null) {
            queryWrapper.eq(Application::getPositionId, positionId);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Application::getStatus, status);
        }

        // 按申请时间排序
        queryWrapper.orderByDesc(Application::getApplyTime);

        return page(page, queryWrapper);
    }

    @Override
    public List<Application> getApplicationsByCandidateId(Long candidateId) {
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getCandidateId, candidateId)
                .orderByDesc(Application::getApplyTime);
        return list(queryWrapper);
    }

    @Override
    public List<Application> getApplicationsByPositionId(Long positionId) {
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getPositionId, positionId)
                .orderByDesc(Application::getApplyTime);
        return list(queryWrapper);
    }

    @Override
    public boolean checkIfApplied(Long candidateId, Long positionId) {
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Application::getCandidateId, candidateId)
                .eq(Application::getPositionId, positionId);
        return count(queryWrapper) > 0;
    }

    @Override
    public Map<String, Long> getStatusStatistics() {
        List<Application> applications = list();
        Map<String, Long> statistics = applications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));
        
        // 确保所有状态都有值
        String[] allStatus = {"PENDING", "REVIEWING", "PASSED", "REJECTED", "INTERVIEW_SCHEDULED"};
        Map<String, Long> result = new HashMap<>();
        for (String status : allStatus) {
            result.put(status, statistics.getOrDefault(status, 0L));
        }
        
        return result;
    }
    
    /**
     * 生成申请编号
     */
    private String generateApplicationNo() {
        // 生成格式：APP + 年月日 + 4位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 生成4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);
        
        return "APP" + dateStr + randomStr;
    }
}