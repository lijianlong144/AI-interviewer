import request from '@/utils/request'

/**
 * 获取面试结果详情
 * @param {number} interviewId - 面试ID
 */
export function getInterviewResult(interviewId) {
  return request({
    url: `/result/${interviewId}`,
    method: 'get'
  })
}

/**
 * 获取面试问题评估结果
 * @param {number} interviewId - 面试ID
 */
export function getQuestionResults(interviewId) {
  return request({
    url: `/result/questions/${interviewId}`,
    method: 'get'
  })
}

/**
 * 获取面试总体评价
 * @param {number} interviewId - 面试ID
 */
export function getOverallEvaluation(interviewId) {
  return request({
    url: `/result/overall/${interviewId}`,
    method: 'get'
  })
}

/**
 * 获取用户面试统计
 * @param {number} userId - 用户ID
 */
export function getUserInterviewStats(userId) {
  return request({
    url: `/result/stats/${userId}`,
    method: 'get'
  })
}

/**
 * 获取面试评分详情
 * @param {number} interviewId - 面试ID
 */
export function getInterviewScores(interviewId) {
  return request({
    url: `/result/scores/${interviewId}`,
    method: 'get'
  })
}

/**
 * 提交面试反馈
 * @param {object} data - 反馈数据
 */
export function submitFeedback(data) {
  return request({
    url: '/result/feedback',
    method: 'post',
    data
  })
} 