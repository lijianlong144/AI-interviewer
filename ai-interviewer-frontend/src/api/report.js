import request from '@/utils/request'

/**
 * 获取面试报告列表
 * @param {Object} params - 查询参数
 * @param {number} [params.current] - 当前页码
 * @param {number} [params.size] - 每页大小
 * @param {string} [params.candidateName] - 候选人姓名
 * @param {string} [params.position] - 职位名称
 * @param {string} [params.startTime] - 开始时间
 * @param {string} [params.endTime] - 结束时间
 */
export function getReportList(params) {
    return request({
        url: '/report/list',
        method: 'get',
        params
    })
}

/**
 * 获取面试报告详情
 * @param {number} id - 报告ID
 */
export function getReportDetail(id) {
    return request({
        url: `/report/detail/${id}`,
        method: 'get'
    })
}

/**
 * 导出面试报告
 * @param {number} id - 报告ID
 */
export function exportReport(id) {
    return request({
        url: `/report/export/${id}`,
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 根据面试ID获取报告
 * @param {number} interviewId - 面试ID
 */
export function getReportByInterviewId(interviewId) {
    return request({
        url: `/report/interview/${interviewId}`,
        method: 'get'
    })
}

/**
 * 更新HR评语
 * @param {number} id - 报告ID
 * @param {string} hrComment - HR评语
 */
export function updateHrComment(id, hrComment) {
    return request({
        url: `/report/comment/${id}`,
        method: 'put',
        params: { hrComment }
    })
}

/**
 * 更新推荐决策
 * @param {number} id - 报告ID
 * @param {string} recommendation - 推荐决策
 */
export function updateRecommendation(id, recommendation) {
    return request({
        url: `/report/recommendation/${id}`,
        method: 'put',
        params: { recommendation }
    })
} 