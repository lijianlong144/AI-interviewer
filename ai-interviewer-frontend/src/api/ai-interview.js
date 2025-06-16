import request from '@/utils/request'

/**
 * 保存面试回答
 * @param {Object} data - 回答数据
 * @param {string} data.answerContent - 回答内容
 * @param {number} data.interviewId - 面试ID
 * @param {number} data.interviewQuestionId - 面试问题ID
 * @param {number} data.thinkDuration - 思考时长(秒)
 * @param {number} data.answerDuration - 回答时长(秒)
 */
export function saveAnswer(data) {
  return request({
    url: '/ai/interview/save-answer',
    method: 'post',
    data: data
  })
}

/**
 * 生成AI面试问题
 * 注意：此API使用SSE，不通过request工具调用
 * @param {number} interviewId - 面试ID
 * @param {string} questionType - 问题类型
 * @param {number} interviewQuestionId - 面试问题ID (必须是数字)
 * @param {boolean} hasContext - 是否有上下文
 * @returns {string} - 返回EventSource URL
 */
export function generateQuestionUrl(interviewId, questionType, interviewQuestionId, hasContext) {
  // 使用相对路径，通过vite代理访问后端
  // 确保interviewQuestionId是数字
  if (typeof interviewQuestionId !== 'number') {
    console.error('interviewQuestionId必须是数字类型');
    interviewQuestionId = parseInt(interviewQuestionId) || Date.now();
  }
  return `/api/ai/interview/generate-stream?interviewId=${interviewId}&questionType=${encodeURIComponent(questionType)}&interviewQuestionId=${interviewQuestionId}&hasContext=${hasContext}`
} 