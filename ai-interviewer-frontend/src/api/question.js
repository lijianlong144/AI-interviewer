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

// 创建题目
export function createQuestion(data) {
  return request({
    url: '/question',
    method: 'post',
    data
  })
}

// 更新题目
export function updateQuestion(data) {
  return request({
    url: '/question',
    method: 'put',
    data
  })
}

// 获取题目详情
export function getQuestionDetail(id) {
  return request({
    url: `/question/${id}`,
    method: 'get'
  })
}

// 删除题目
export function deleteQuestion(id) {
  return request({
    url: `/question/${id}`,
    method: 'delete'
  })
}

// 分页查询题目
export function getQuestionPage(params) {
  return request({
    url: '/question/page',
    method: 'get',
    params
  })
}

// 获取随机题目
export function getRandomQuestions(position) {
  return request({
    url: '/question/random',
    method: 'get',
    params: { position }
  })
}

// 获取推荐题目
export function getRecommendQuestions(position, count = 10) {
  return request({
    url: '/question/recommend',
    method: 'get',
    params: { position, count }
  })
}

// 更新题目状态
export function updateQuestionStatus(questionId, status) {
  return request({
    url: '/question/status',
    method: 'post',
    params: { questionId, status }
  })
}

// 增加问题使用次数
export function incrementQuestionUseCount(id) {
  return request({
    url: `/question/use/${id}`,
    method: 'post'
  })
} 