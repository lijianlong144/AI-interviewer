import request from '@/utils/request'

/**
 * 创建在线简历
 */
export function createOnlineResume(data) {
    return request({
        url: '/resume',
        method: 'post',
        data
    })
}

/**
 * 更新简历
 */
export function updateResume(data) {
    return request({
        url: '/resume',
        method: 'put',
        data
    })
}

/**
 * 获取简历详情
 */
export function getResumeDetail(id) {
    return request({
        url: `/resume/${id}`,
        method: 'get'
    })
}

/**
 * 删除简历
 */
export function deleteResume(id) {
    return request({
        url: `/resume/${id}`,
        method: 'delete'
    })
}

/**
 * 获取当前用户的所有简历
 */
export function getMyResumes() {
    return request({
        url: '/resume/my',
        method: 'get'
    })
}

/**
 * 分页查询简历（管理员接口）
 */
export function getResumePage(params) {
    return request({
        url: '/resume/page',
        method: 'get',
        params
    })
}

/**
 * 上传简历文件
 * @param {FormData} formData - 包含文件的FormData对象
 */
export function uploadResume(formData) {
    return request({
        url: '/resume/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 管理员根据简历id获取简历
 */
export function getResumeByAdmin(id) {
    return request({
        url: `/resume/admin/${id}`,
        method: 'get'
    })
}