import request from '@/utils/request'

/**
 * HR发布职位
 */
export function publishPosition(data) {
    return request({
        url: '/interview-process/position/publish',
        method: 'post',
        data
    })
}

/**
 * 候选人投递简历
 * @param {number} positionId - 职位ID
 * @param {number} resumeId - 简历ID
 */
export function applyPosition(positionId, resumeId) {
    return request({
        url: '/interview-process/apply',
        method: 'post',
        params: { positionId, resumeId }
    })
}

/**
 * HR初筛简历
 * @param {number} applicationId - 申请ID
 * @param {string} status - 状态：PASSED/REJECTED
 * @param {string} remark - 备注
 */
export function screenResume(applicationId, status, remark) {
    return request({
        url: `/interview-process/screen/${applicationId}`,
        method: 'put',
        params: { status, remark }
    })
}

/**
 * 安排面试
 * @param {number} applicationId - 申请ID
 * @param {number} interviewerId - 面试官ID
 * @param {string} scheduledTime - 预约时间
 * @param {number} duration - 面试时长（分钟）
 */
export function scheduleInterview(applicationId, interviewerId, scheduledTime, duration) {
    return request({
        url: '/interview-process/schedule',
        method: 'post',
        params: {
            applicationId,
            interviewerId,
            scheduledTime,
            duration
        }
    })
}

/**
 * 发送面试邀请邮件
 * @param {number} interviewId - 面试ID
 */
export function sendInterviewInvitation(interviewId) {
    return request({
        url: `/interview-process/invitation/${interviewId}`,
        method: 'post'
    })
}

/**
 * 候选人确认面试
 * @param {number} interviewId - 面试ID
 * @param {boolean} confirmed - 是否确认
 * @param {string} reason - 拒绝理由（可选）
 */
export function confirmInterview(interviewId, confirmed, reason) {
    return request({
        url: `/interview-process/confirm/${interviewId}`,
        method: 'put',
        params: { confirmed, reason }
    })
}

/**
 * 开始面试
 * @param {number} interviewId - 面试ID
 */
export function startInterview(interviewId) {
    return request({
        url: `/interview-process/start/${interviewId}`,
        method: 'put'
    })
}

/**
 * 结束面试
 * @param {number} interviewId - 面试ID
 */
export function endInterview(interviewId) {
    return request({
        url: `/interview-process/end/${interviewId}`,
        method: 'put'
    })
}

/**
 * 生成面试报告
 * @param {number} interviewId - 面试ID
 */
export function generateInterviewReport(interviewId) {
    return request({
        url: `/interview-process/report/${interviewId}`,
        method: 'post'
    })
}

/**
 * 发送面试结果通知
 * @param {number} interviewId - 面试ID
 * @param {string} result - 结果：PASS/FAIL
 * @param {string} feedback - 反馈
 * @param {string} nextSteps - 下一步（可选）
 */
export function sendInterviewResult(interviewId, result, feedback, nextSteps) {
    return request({
        url: `/interview-process/result/${interviewId}`,
        method: 'post',
        params: { result, feedback, nextSteps }
    })
}