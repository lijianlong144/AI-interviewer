import request from '@/utils/request'

/**
 * 创建职位申请
 */
export function createApplication(data) {
    return request({
        url: '/application',
        method: 'post',
        data
    })
}

/**
 * 获取申请详情
 */
export function getApplicationDetail(id) {
    return request({
        url: `/application/${id}`,
        method: 'get'
    })
}

/**
 * 更新申请状态
 * @param {number} id - 申请ID
 * @param {string} status - 状态
 * @param {string} remark - 备注
 */
export function updateApplicationStatus(id, status, remark) {
    return request({
        url: `/application/status/${id}`,
        method: 'put',
        params: { status, remark }
    })
}

/**
 * 获取当前用户的所有申请
 */
export function getMyApplications() {
    return request({
        url: '/application/my',
        method: 'get'
    })
}

/**
 * 检查当前用户是否已申请职位
 * @param {number} positionId - 职位ID
 */
export function checkApplication(params) {
    return request({
        url: '/application/check',
        method: 'get',
        params
    })
}

/**
 * 获取职位的所有申请（HR操作）
 * @param {number} positionId - 职位ID
 */
export function getPositionApplications(positionId) {
    return request({
        url: `/application/position/${positionId}`,
        method: 'get'
    })
}

/**
 * 分页查询申请列表
 */
export function getApplicationPage(params) {
    return request({
        url: '/application/page',
        method: 'get',
        params
    })
}

/**
 * 获取申请状态统计（HR操作）
 */
export function getApplicationStats() {
    return request({
        url: '/application/stats',
        method: 'get'
    })
}