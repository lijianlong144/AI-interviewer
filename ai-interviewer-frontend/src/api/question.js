import request from '@/utils/request'

/**
 * 获取面试问题列表
 * @param {number} interviewId - 面试ID
 */
export function getInterviewQuestions(interviewId) {
  return request({
    url: `/question/interview/${interviewId}`,
    method: 'get'
  })
}

/**
 * 获取下一个面试问题
 * @param {number} interviewId - 面试ID
 */
export function getNextQuestion(interviewId) {
  return request({
    url: `/question/next/${interviewId}`,
    method: 'get'
  })
}

/**
 * 提交问题回答
 * @param {object} data - 回答数据
 */
export function submitAnswer(data) {
  return request({
    url: '/question/answer',
    method: 'post',
    data
  })
}

/**
 * 获取AI评估结果
 * @param {number} answerId - 回答ID
 */
export function getAnswerEvaluation(answerId) {
  return request({
    url: `/question/evaluation/${answerId}`,
    method: 'get'
  })
}

/**
 * 请求AI面试官提示
 * @param {number} interviewId - 面试ID
 * @param {number} questionId - 问题ID
 */
export function requestAIHint(interviewId, questionId) {
  return request({
    url: '/question/hint',
    method: 'get',
    params: {
      interviewId,
      questionId
    }
  })
} 