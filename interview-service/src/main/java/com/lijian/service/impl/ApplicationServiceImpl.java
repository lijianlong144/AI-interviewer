package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Application;
import com.lijian.entity.Interview;
import com.lijian.entity.Position;
import com.lijian.entity.Resume;
import com.lijian.entity.SysUser;
import com.lijian.entity.dto.ApplicationDTO;
import com.lijian.mapper.ApplicationMapper;
import com.lijian.service.ApplicationService;
import com.lijian.service.InterviewService;
import com.lijian.service.PositionService;
import com.lijian.service.ResumeService;
import com.lijian.service.SysUserService;
import com.lijian.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 职位申请服务实现类
 */
@Service
@Slf4j
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private PositionService positionService;

    @Autowired
    private ResumeService resumeService;
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private InterviewService interviewService;

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
    @Transactional
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
            
            // 如果通过初筛，自动创建面试
            if ("PASSED".equals(status)) {
                createInterviewFromApplication(application);
            }
        }

        return updateById(application);
    }
    
    /**
     * 从申请创建面试
     * @param application 申请信息
     */
    private void createInterviewFromApplication(Application application) {
        try {
            // 获取职位信息
            Position position = positionService.getById(application.getPositionId());
            if (position == null) {
                return;
            }
            
            // 获取候选人信息
            SysUser candidate = sysUserService.getById(application.getCandidateId());
            if (candidate == null) {
                return;
            }
            
            // 创建面试
            Interview interview = new Interview();
            
            // 设置基本信息
            interview.setCandidateId(application.getCandidateId());
            interview.setPosition(position.getTitle());
            interview.setPositionId(position.getId());
            interview.setApplicationId(application.getId());
            
            // 设置简历ID
            interview.setResumeId(application.getResumeId());
            
            // 设置创建人为当前操作的HR
            Long currentUserId = SecurityUtils.getCurrentUserId();
            interview.setCreatorId(currentUserId);
            
            // 设置面试状态为待进行
            interview.setStatus("PENDING");
            
            // 生成面试房间号
            interview.setRoomCode(interviewService.generateRoomCode());
            
            // 设置默认预约时间为当前时间后1天
            interview.setScheduledTime(LocalDateTime.now().plusDays(1));
            
            // 设置默认面试时长为30分钟
            interview.setDuration(30);
            
            // 保存面试
            interviewService.createInterview(interview);
            
            log.info("成功为申请ID={}创建面试，面试ID={}，包含简历ID={}", 
                    application.getId(), interview.getId(), application.getResumeId());
            
        } catch (Exception e) {
            // 记录错误但不影响申请状态更新
            log.error("创建面试失败", e);
        }
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
    public Page<ApplicationDTO> pageApplicationsWithDetails(Page<Application> page, Long candidateId, String candidateName, Long positionId, String status) {
        // 1. 先查询Application基础数据
        LambdaQueryWrapper<Application> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (candidateId != null) {
            queryWrapper.eq(Application::getCandidateId, candidateId);
        }
        
        if (positionId != null) {
            queryWrapper.eq(Application::getPositionId, positionId);
        }
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Application::getStatus, status);
        }
        
        // 如果传入了候选人姓名，需要先查询对应的用户ID
        if (StringUtils.hasText(candidateName)) {
            LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(SysUser::getRealName, candidateName);
            List<SysUser> users = sysUserService.list(userQueryWrapper);
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
                queryWrapper.in(Application::getCandidateId, userIds);
            } else {
                // 如果没有找到匹配的用户，直接返回空结果
                return new Page<>(page.getCurrent(), page.getSize());
            }
        }
        
        // 按申请时间排序
        queryWrapper.orderByDesc(Application::getApplyTime);
        
        // 执行分页查询
        Page<Application> applicationPage = page(page, queryWrapper);
        List<Application> records = applicationPage.getRecords();
        
        // 如果没有记录，直接返回空结果
        if (records.isEmpty()) {
            return new Page<ApplicationDTO>(page.getCurrent(), page.getSize());
        }
        
        // 2. 收集所有的职位ID和候选人ID
        List<Long> positionIds = records.stream()
                .map(Application::getPositionId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> candidateIds = records.stream()
                .map(Application::getCandidateId)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量查询职位和用户信息
        Map<Long, String> positionTitleMap = new HashMap<>();
        if (!positionIds.isEmpty()) {
            LambdaQueryWrapper<Position> positionQueryWrapper = new LambdaQueryWrapper<>();
            positionQueryWrapper.in(Position::getId, positionIds);
            List<Position> positions = positionService.list(positionQueryWrapper);
            positionTitleMap = positions.stream()
                    .collect(Collectors.toMap(Position::getId, Position::getTitle));
        }
        
        Map<Long, String> candidateNameMap = new HashMap<>();
        if (!candidateIds.isEmpty()) {
            LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.in(SysUser::getId, candidateIds);
            List<SysUser> users = sysUserService.list(userQueryWrapper);
            candidateNameMap = users.stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
        }
        
        // 4. 组装ApplicationDTO对象
        List<ApplicationDTO> dtoList = new ArrayList<>(records.size());
        for (Application application : records) {
            ApplicationDTO dto = new ApplicationDTO();
            BeanUtils.copyProperties(application, dto);
            
            // 设置职位名称
            dto.setPositionTitle(positionTitleMap.getOrDefault(application.getPositionId(), ""));
            
            // 设置候选人姓名
            dto.setCandidateName(candidateNameMap.getOrDefault(application.getCandidateId(), ""));
            
            dtoList.add(dto);
        }
        
        // 5. 创建并返回结果页
        Page<ApplicationDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(dtoList);
        
        return resultPage;
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