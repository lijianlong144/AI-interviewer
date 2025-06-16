package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Interview;
import com.lijian.enums.InterviewStatusEnum;
import com.lijian.exception.BusinessException;
import com.lijian.mapper.InterviewMapper;
import com.lijian.service.InterviewService;
import com.lijian.dto.InterviewDTO;
import com.lijian.entity.SysUser;
import com.lijian.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * 面试服务实现类
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Interview createInterview(Interview interview) {
        // 生成面试编号
        if (!StringUtils.hasText(interview.getInterviewNo())) {
            interview.setInterviewNo(generateInterviewNo());
        }

        // 生成房间号
        if (!StringUtils.hasText(interview.getRoomCode())) {
            interview.setRoomCode(generateRoomCode());
        }

        // 设置默认状态
        if (!StringUtils.hasText(interview.getStatus())) {
            interview.setStatus(InterviewStatusEnum.PENDING.getCode());
        }
        // 因为没有面试官这个角色了，所以这段代码用不到了
//        // 检查时间冲突
//        if (interview.getScheduledTime() != null) {
//            boolean hasConflict = checkTimeConflict(
//                    interview.getCandidateId(),
//                    interview.getInterviewerId(),
//                    interview.getScheduledTime(),
//                    60 // 默认预计1小时
//            );
//            if (hasConflict) {
//                throw new BusinessException("面试时间冲突，请选择其他时间");
//            }
//        }

        save(interview);
        return interview;
    }

    @Override
    public List<Interview> getByCandidateId(Long candidateId) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getCandidateId, candidateId);
        queryWrapper.orderByDesc(Interview::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<Interview> getByInterviewerId(Long interviewerId) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getInterviewerId, interviewerId);
        queryWrapper.orderByDesc(Interview::getScheduledTime);
        return list(queryWrapper);
    }

    @Override
    public List<Interview> getByStatus(String status) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getStatus, status);
        queryWrapper.orderByAsc(Interview::getScheduledTime);
        return list(queryWrapper);
    }

    @Override
    public Page<Interview> pageInterviews(Page<Interview> page, String status, String position, Long candidateId, Long interviewerId) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Interview::getStatus, status);
        }

        if (StringUtils.hasText(position)) {
            queryWrapper.like(Interview::getPosition, position);
        }

        if (candidateId != null) {
            queryWrapper.eq(Interview::getCandidateId, candidateId);
        }

        if (interviewerId != null) {
            queryWrapper.eq(Interview::getInterviewerId, interviewerId);
        }

        queryWrapper.orderByDesc(Interview::getScheduledTime);

        return page(page, queryWrapper);
    }

    @Override
    public boolean startInterview(Long id) {
        Interview interview = getById(id);
        if (interview == null) {
            throw new BusinessException("面试不存在");
        }

        if (!InterviewStatusEnum.PENDING.getCode().equals(interview.getStatus())) {
            throw new BusinessException("只有待进行的面试才能开始");
        }

        interview.setStatus(InterviewStatusEnum.ONGOING.getCode());
        interview.setStartTime(LocalDateTime.now());
        return updateById(interview);
    }

    @Override
    public boolean endInterview(Long id) {
        Interview interview = getById(id);
        if (interview == null) {
            throw new BusinessException("面试不存在");
        }

        if (!InterviewStatusEnum.ONGOING.getCode().equals(interview.getStatus())) {
            throw new BusinessException("只有进行中的面试才能结束");
        }

        LocalDateTime endTime = LocalDateTime.now();
        interview.setStatus(InterviewStatusEnum.COMPLETED.getCode());
        interview.setEndTime(endTime);

        // 计算面试时长（分钟）
        if (interview.getStartTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(interview.getStartTime(), endTime);
            interview.setDuration((int) minutes);
        } else {
            throw new BusinessException("面试开始时间未记录，无法结束面试，系统内部错误，请联系相关hr重新面试");
        }

        return updateById(interview);
    }

    @Override
    public boolean cancelInterview(Long id, String reason) {
        Interview interview = getById(id);
        if (interview == null) {
            throw new BusinessException("面试不存在");
        }

        if (InterviewStatusEnum.COMPLETED.getCode().equals(interview.getStatus()) ||
                InterviewStatusEnum.CANCELLED.getCode().equals(interview.getStatus())) {
            throw new BusinessException("已完成或已取消的面试无法再次取消");
        }

        interview.setStatus(InterviewStatusEnum.CANCELLED.getCode());
        // 可以记录取消原因，如果数据库有对应字段的话

        return updateById(interview);
    }

    @Override
    public Interview getByRoomCode(String roomCode) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getRoomCode, roomCode);
        return getOne(queryWrapper);
    }

    @Override
    public String checkInterviewStatus(Long id) {
        Interview interview = getById(id);
        if (interview == null) {
            throw new BusinessException("面试不存在");
        }
        return interview.getStatus();
    }

    @Override
    public List<Interview> getTodayInterviews(Long interviewerId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.ge(Interview::getScheduledTime, today.atStartOfDay());
        queryWrapper.lt(Interview::getScheduledTime, today.plusDays(1).atStartOfDay());

        if (interviewerId != null) {
            queryWrapper.eq(Interview::getInterviewerId, interviewerId);
        }

        queryWrapper.orderByAsc(Interview::getScheduledTime);
        return list(queryWrapper);
    }

    @Override
    public List<Interview> getUpcomingInterviews(Long userId, Integer hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);

        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Interview::getScheduledTime, now, endTime);

        if (userId != null) {
            queryWrapper.and(wrapper ->
                    wrapper.eq(Interview::getCandidateId, userId)
                            .or()
                            .eq(Interview::getInterviewerId, userId)
            );
        }

        queryWrapper.ne(Interview::getStatus, InterviewStatusEnum.CANCELLED.getCode());
        queryWrapper.orderByAsc(Interview::getScheduledTime);

        return list(queryWrapper);
    }

    @Override
    public boolean rescheduleInterview(Long id, LocalDateTime newTime, String reason) {
        Interview interview = getById(id);
        if (interview == null) {
            throw new BusinessException("面试不存在");
        }
// 应该要改，以及TODO： 60是写死的默认值，后续要改，从外面传进来
        if (InterviewStatusEnum.COMPLETED.getCode().equals(interview.getStatus()) ||
                InterviewStatusEnum.CANCELLED.getCode().equals(interview.getStatus())) {
            throw new BusinessException("已完成或已取消的面试无法重新安排");
        }

        // 检查新时间是否有冲突
        boolean hasConflict = checkTimeConflict(
                interview.getCandidateId(),
                interview.getInterviewerId(),
                newTime,
                60
        );
        if (hasConflict) {
            throw new BusinessException("新的面试时间冲突，请选择其他时间");
        }

        interview.setScheduledTime(newTime);
        interview.setStatus(InterviewStatusEnum.PENDING.getCode()); // 重置为待进行状态

        return updateById(interview);
    }

    @Override
    public Object getInterviewStatistics(Long interviewerId, String startDate, String endDate) {
        // 这里返回一个简单的统计对象，实际项目中可能需要更复杂的统计逻辑
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();

        if (interviewerId != null) {
            queryWrapper.eq(Interview::getInterviewerId, interviewerId);
        }

        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge(Interview::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }

        if (StringUtils.hasText(endDate)) {
            queryWrapper.le(Interview::getCreateTime, LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }

        List<Interview> interviews = list(queryWrapper);

        statistics.put("total", interviews.size());
        statistics.put("pending", interviews.stream().filter(i -> InterviewStatusEnum.PENDING.getCode().equals(i.getStatus())).count());
        statistics.put("ongoing", interviews.stream().filter(i -> InterviewStatusEnum.ONGOING.getCode().equals(i.getStatus())).count());
        statistics.put("completed", interviews.stream().filter(i -> InterviewStatusEnum.COMPLETED.getCode().equals(i.getStatus())).count());
        statistics.put("cancelled", interviews.stream().filter(i -> InterviewStatusEnum.CANCELLED.getCode().equals(i.getStatus())).count());

        return statistics;
    }

    @Override
    public boolean batchUpdateStatus(List<Long> interviewIds, String status) {
        if (interviewIds == null || interviewIds.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Interview::getId, interviewIds);
        List<Interview> interviews = list(queryWrapper);

        interviews.forEach(interview -> interview.setStatus(status));
        return updateBatchById(interviews);
    }

    @Override
    public String generateRoomCode() {
        // 生成6位数字作为中间部分
        int randomDigits = new Random().nextInt(900000) + 100000; // 生成100000到999999之间的随机数
        
        // 生成8位随机字母数字混合字符串作为后缀
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        
        // 组合成格式: IV-6位数字-8位字母数字
        String roomCode = "IV-" + String.format("%06d", randomDigits) + "-" + sb.toString();
        
        // 检查房间号是否已存在
        while (getByRoomCode(roomCode) != null) {
            // 重新生成
            randomDigits = new Random().nextInt(900000) + 100000;
            sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            roomCode = "IV-" + String.format("%06d", randomDigits) + "-" + sb.toString();
        }
        
        return roomCode;
    }

    @Override
    public boolean checkTimeConflict(Long candidateId, Long interviewerId, LocalDateTime scheduledTime, Integer duration) {
        LocalDateTime startTime = scheduledTime;
        LocalDateTime endTime = scheduledTime.plusMinutes(duration);

        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();

        // 检查候选人和面试官的时间冲突
        queryWrapper.and(wrapper ->
                wrapper.eq(Interview::getCandidateId, candidateId)
                        .or()
                        .eq(Interview::getInterviewerId, interviewerId)
        );

        // 排除已取消的面试
        queryWrapper.ne(Interview::getStatus, InterviewStatusEnum.CANCELLED.getCode());

        List<Interview> existingInterviews = list(queryWrapper);

        // 检查每个已存在的面试是否与新面试时间重叠
        for (Interview interview : existingInterviews) {
            LocalDateTime existingStart = interview.getScheduledTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(interview.getDuration()); // 假设有duration字段

            // 时间重叠判断：两个时间段重叠的条件
            // 新面试开始时间 < 已存在面试结束时间 AND 新面试结束时间 > 已存在面试开始时间
            if (startTime.isBefore(existingEnd) && endTime.isAfter(existingStart)) {
                return true; // 存在冲突
            }
        }

        return false; // 无冲突
    }

    @Override
    public Integer calculateInterviewDuration(Long interviewId) {
        Interview interview = getById(interviewId);
        if (interview == null || interview.getStartTime() == null || interview.getEndTime() == null) {
            return 0;
        }

        return (int) ChronoUnit.MINUTES.between(interview.getStartTime(), interview.getEndTime());
    }

    @Override
    public int autoCancelTimeoutInterviews(Integer timeoutMinutes) {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);

        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getStatus, InterviewStatusEnum.PENDING.getCode());
        queryWrapper.le(Interview::getScheduledTime, timeoutTime);

        List<Interview> timeoutInterviews = list(queryWrapper);

        if (!timeoutInterviews.isEmpty()) {
            timeoutInterviews.forEach(interview ->
                    interview.setStatus(InterviewStatusEnum.CANCELLED.getCode())
            );
            updateBatchById(timeoutInterviews);
        }

        return timeoutInterviews.size();
    }

    /**
     * 生成面试编号
     */
    private String generateInterviewNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.toString().replace("-", "");

        // 查询今日已创建的面试数量
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Interview::getCreateTime, today.atStartOfDay());
        queryWrapper.lt(Interview::getCreateTime, today.plusDays(1).atStartOfDay());
        long todayCount = count(queryWrapper);

        // 生成序号（4位数，不足补0）
        String sequence = String.format("%04d", todayCount + 1);

        return "IV" + dateStr + sequence;
    }

    @Override
    public boolean startInterviewByRoomCode(String roomCode) {
        // 1. 根据roomCode查询面试信息
        Interview interview = getByRoomCode(roomCode);
        if (interview == null) {
            throw new BusinessException("面试不存在，请检查房间号");
        }
        
        // 2. 判断面试状态
        String status = interview.getStatus();
        if (InterviewStatusEnum.ONGOING.getCode().equals(status)) {
            throw new BusinessException("面试已经开始，无需重复操作");
        } else if (InterviewStatusEnum.COMPLETED.getCode().equals(status)) {
            throw new BusinessException("面试已经结束，无法开始");
        } else if (InterviewStatusEnum.CANCELLED.getCode().equals(status)) {
            throw new BusinessException("面试已取消，无法开始");
        }
        
        // 3. 调用现有的开始面试方法
        return startInterview(interview.getId());
    }

    @Override
    public boolean endInterviewByRoomCode(String roomCode) {
        // 1. 根据roomCode查询面试信息
        Interview interview = getByRoomCode(roomCode);
        if (interview == null) {
            throw new BusinessException("面试不存在，请检查房间号");
        }
        
        // 2. 判断面试状态
        String status = interview.getStatus();
        if (InterviewStatusEnum.PENDING.getCode().equals(status)) {
            throw new BusinessException("面试尚未开始，无法结束");
        } else if (InterviewStatusEnum.COMPLETED.getCode().equals(status)) {
            throw new BusinessException("面试已经结束，无需重复操作");
        } else if (InterviewStatusEnum.CANCELLED.getCode().equals(status)) {
            throw new BusinessException("面试已取消，无法结束");
        }
        
        // 3. 调用现有的结束面试方法
        return endInterview(interview.getId());
    }

    @Override
    public Page<InterviewDTO> pageInterviewsWithCandidate(Page<InterviewDTO> page, String status, String position, 
                                                       Long candidateId, Long interviewerId, String candidateName) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Interview::getStatus, status);
        }
        
        if (StringUtils.hasText(position)) {
            queryWrapper.like(Interview::getPosition, position);
        }
        
        if (candidateId != null) {
            queryWrapper.eq(Interview::getCandidateId, candidateId);
        }
        
        // 按照用户要求，忽略interviewerId字段
        
        queryWrapper.orderByDesc(Interview::getScheduledTime);
        
        // 2. 如果提供了候选人姓名，先查询用户ID
        if (StringUtils.hasText(candidateName)) {
            LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(SysUser::getRealName, candidateName);
            List<SysUser> users = sysUserMapper.selectList(userQueryWrapper);
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
                queryWrapper.in(Interview::getCandidateId, userIds);
            } else {
                // 如果没有找到匹配的用户，返回空结果
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
        }
        
        // 3. 执行分页查询
        Page<Interview> interviewPage = new Page<>(page.getCurrent(), page.getSize());
        Page<Interview> resultPage = page(interviewPage, queryWrapper);
        
        // 4. 转换为DTO并填充候选人姓名
        Page<InterviewDTO> dtoPage = new Page<>(
            resultPage.getCurrent(), 
            resultPage.getSize(), 
            resultPage.getTotal()
        );
        
        if (resultPage.getRecords().isEmpty()) {
            dtoPage.setRecords(new ArrayList<>());
            return dtoPage;
        }
        
        // 5. 获取所有候选人ID
        List<Long> candidateIds = resultPage.getRecords().stream()
            .map(Interview::getCandidateId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        // 6. 批量查询候选人信息
        final Map<Long, String> candidateNameMap = new HashMap<>();
        if (!candidateIds.isEmpty()) {
            LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.in(SysUser::getId, candidateIds);
            List<SysUser> candidates = sysUserMapper.selectList(userQueryWrapper);
            
            candidateNameMap.putAll(candidates.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName)));
        }
        
        // 7. 转换并设置候选人姓名
        List<InterviewDTO> dtoList = resultPage.getRecords().stream().map(interview -> {
            InterviewDTO dto = new InterviewDTO();
            BeanUtils.copyProperties(interview, dto);
            
            // 设置候选人姓名
            if (interview.getCandidateId() != null) {
                dto.setCandidateName(candidateNameMap.getOrDefault(interview.getCandidateId(), ""));
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public Page<Interview> pageCandidateInterviews(Page<Interview> page, Long candidateId, String status, 
                                                 String keyword, String startDate, String endDate) {
        // 构建查询条件
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据候选人ID查询
        queryWrapper.eq(Interview::getCandidateId, candidateId);
        
        // 根据状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Interview::getStatus, status);
        }
        
        // 根据关键词搜索（面试编号或职位）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> 
                wrapper.like(Interview::getInterviewNo, keyword)
                       .or()
                       .like(Interview::getPosition, keyword)
            );
        }
        
        // 根据日期范围筛选
        if (StringUtils.hasText(startDate)) {
            LocalDate start = LocalDate.parse(startDate);
            queryWrapper.ge(Interview::getScheduledTime, start.atStartOfDay());
        }
        
        if (StringUtils.hasText(endDate)) {
            LocalDate end = LocalDate.parse(endDate);
            queryWrapper.le(Interview::getScheduledTime, end.plusDays(1).atStartOfDay());
        }
        
        // 按预约时间降序排序
        queryWrapper.orderByDesc(Interview::getScheduledTime);
        
        // 执行分页查询
        return page(page, queryWrapper);
    }
}