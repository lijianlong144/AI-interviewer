package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.Application;

import java.util.List;
import java.util.Map;

/**
 * 职位申请服务接口
 */
public interface ApplicationService extends IService<Application> {

    /**
     * 创建职位申请
     * @param application 申请信息
     * @return 创建后的申请
     */
    Application createApplication(Application application);

    /**
     * 更新申请状态
     * @param id 申请ID
     * @param status 状态：PENDING/REVIEWING/PASSED/REJECTED/INTERVIEW_SCHEDULED/OFFER_SENT/ACCEPTED/DECLINED
     * @param remark 备注
     * @return 是否成功
     */
    boolean updateStatus(Long id, String status, String remark);

    /**
     * 分页查询申请列表
     * @param page 分页参数
     * @param candidateId 候选人ID
     * @param positionId 职位ID
     * @param status 状态
     * @return 申请分页列表
     */
    Page<Application> pageApplications(Page<Application> page, Long candidateId, Long positionId, String status);

    /**
     * 获取候选人的所有申请
     * @param candidateId 候选人ID
     * @return 申请列表
     */
    List<Application> getApplicationsByCandidateId(Long candidateId);

    /**
     * 获取职位的所有申请
     * @param positionId 职位ID
     * @return 申请列表
     */
    List<Application> getApplicationsByPositionId(Long positionId);

    /**
     * 检查是否已申请
     * @param candidateId 候选人ID
     * @param positionId 职位ID
     * @return 是否已申请
     */
    boolean checkIfApplied(Long candidateId, Long positionId);

    /**
     * 获取申请状态统计
     * @return 状态统计
     */
    Map<String, Long> getStatusStatistics();
}