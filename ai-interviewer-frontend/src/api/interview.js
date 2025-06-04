import request from '@/utils/request'

/**
 * 获取候选人的面试列表
 * @param {number} candidateId - 候选人ID
 */
export function getCandidateInterviews(candidateId) {
  return request({
    url: `/interview/candidate/${candidateId}`,
    method: 'get'
  })
}

/**
 * 获取面试详情
 * @param {number} interviewId - 面试ID
 */
export function getInterviewDetail(interviewId) {
  return request({
    url: `/interview/${interviewId}`,
    method: 'get'
  })
}

/**
 * 检查面试状态
 * @param {number} interviewId - 面试ID
 */
export function checkInterviewStatus(interviewId) {
  return request({
    url: `/interview/check-status/${interviewId}`,
    method: 'get'
  })
}

/**
 * 开始面试
 * @param {number} interviewId - 面试ID
 */
export function startInterview(interviewId) {
  return request({
    url: `/interview/start/${interviewId}`,
    method: 'post'
  })
}

/**
 * 结束面试
 * @param {number} interviewId - 面试ID
 */
export function endInterview(interviewId) {
  return request({
    url: `/interview/end/${interviewId}`,
    method: 'post'
  })
}

/**
 * 获取即将开始的面试
 * @param {number} userId - 用户ID
 * @param {number} hours - 未来小时数
 */
export function getUpcomingInterviews(userId, hours = 24) {
  return request({
    url: `/interview/upcoming`,
    method: 'get',
    params: { userId, hours }
  })
}

/**
 * 获取今日面试
 * @param {number} candidateId - 候选人ID
 */
export function getTodayInterviews(candidateId) {
  return request({
    url: `/interview/today`,
    method: 'get',
    params: { candidateId }
  })
}

/**
 * 取消面试
 * @param {number} interviewId - 面试ID
 */
export function cancelInterview(interviewId) {
  return request({
    url: `/interview/cancel/${interviewId}`,
    method: 'post'
  })
} 