import request from '@/utils/request'

/**
 * 获取申请统计数据
 */
export function getApplicationStats() {
    return request({
        url: '/application/stats',
        method: 'get'
    })
}

/**
 * 获取面试统计数据
 */
export function getInterviewStats() {
    return request({
        url: '/interview/stats',
        method: 'get'
    })
}

/**
 * 获取职位统计数据
 */
export function getPositionStats() {
    return request({
        url: '/position/stats',
        method: 'get'
    })
}

/**
 * 获取候选人仪表盘数据
 */
export function getCandidateDashboardStats() {
    return request({
        url: '/stats/candidate/dashboard',
        method: 'get'
    })
}

/**
 * 获取HR仪表盘数据
 */
export function getHRDashboardStats() {
    return request({
        url: '/stats/hr/dashboard',
        method: 'get'
    })
}

/**
 * 获取面试官仪表盘数据
 */
export function getInterviewerDashboardStats() {
    return request({
        url: '/stats/interviewer/dashboard',
        method: 'get'
    })
} 