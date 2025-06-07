import request from '@/utils/request'

/**
 * 创建职位
 */
export function createPosition(data) {
    return request({
        url: '/position',
        method: 'post',
        data
    })
}

/**
 * 更新职位
 */
export function updatePosition(data) {
    return request({
        url: '/position',
        method: 'put',
        data
    })
}

/**
 * 获取职位详情
 */
export function getPositionDetail(id) {
    return request({
        url: `/position/${id}`,
        method: 'get'
    })
}

/**
 * 删除职位
 */
export function deletePosition(id) {
    return request({
        url: `/position/${id}`,
        method: 'delete'
    })
}

/**
 * 更新职位状态
 * @param {number} id - 职位ID
 * @param {number} status - 状态：1-招聘中 2-已关闭 3-已暂停
 */
export function updatePositionStatus(id, status) {
    return request({
        url: `/position/status/${id}`,
        method: 'put',
        params: { status }
    })
}

/**
 * 分页查询职位
 */
export function getPositionPage(params) {
    return request({
        url: '/position/page',
        method: 'get',
        params
    })
}

/**
 * 获取热门职位
 * @param {number} limit - 数量限制
 */
export function getHotPositions(limit = 10) {
    return request({
        url: '/position/hot',
        method: 'get',
        params: { limit }
    })
}

/**
 * 根据部门获取职位
 * @param {string} department - 部门名称
 */
export function getPositionsByDepartment(department) {
    return request({
        url: '/position/department',
        method: 'get',
        params: { department }
    })
}

/**
 * 获取最新职位
 * @param {number} limit - 数量限制
 */
export function getLatestPositions(limit = 10) {
    return request({
        url: '/position/latest',
        method: 'get',
        params: { limit }
    })
}