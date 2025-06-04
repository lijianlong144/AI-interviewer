import request from '@/utils/request'

/**
 * 获取用户信息
 * @param {number} userId - 用户ID
 */
export function getUserInfo(userId) {
  return request({
    url: `/user/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {object} userInfo - 用户信息对象
 */
export function updateUserInfo(userInfo) {
  return request({
    url: '/user',
    method: 'put',
    data: userInfo
  })
}

/**
 * 修改密码
 * @param {number} userId - 用户ID
 * @param {string} oldPassword - 旧密码
 * @param {string} newPassword - 新密码
 */
export function changePassword(userId, oldPassword, newPassword) {
  return request({
    url: '/user/change-password',
    method: 'post',
    params: {
      userId,
      oldPassword,
      newPassword
    }
  })
}

/**
 * 用户注册
 * @param {object} data - 注册信息对象
 */
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 获取用户分页列表
 * @param {object} params - 分页参数
 */
export function getUserPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态
 * @param {number} id - 用户ID
 * @param {number} status - 状态值
 */
export function updateUserStatus(id, status) {
  return request({
    url: '/user/status',
    method: 'post',
    params: {
      id,
      status
    }
  })
} 