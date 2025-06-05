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
    url: '/interview/upcoming',
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
    url: '/interview/today',
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

// 创建面试
export function createInterview(data) {
  return request({
    url: '/interview',
    method: 'post',
    data
  })
}

// 更新面试信息
export function updateInterview(data) {
  return request({
    url: '/interview',
    method: 'put',
    data
  })
}

// 分页查询面试
export function getInterviewPage(params) {
  return request({
    url: '/interview/page',
    method: 'get',
    params
  })
}

// 获取面试统计信息
export function getInterviewStatistics(params) {
  return request({
    url: '/interview/statistics',
    method: 'get',
    params
  })
}

// 根据面试官ID获取面试列表
export function getInterviewsByInterviewer(interviewerId) {
  return request({
    url: `/interview/interviewer/${interviewerId}`,
    method: 'get'
  })
}

// 根据候选人ID获取面试列表
export function getInterviewsByCandidate(candidateId) {
  return request({
    url: `/interview/candidate/${candidateId}`,
    method: 'get'
  })
} 