import request from '@/utils/request'

/**
 * 发送简单邮件
 * @param {string} to - 收件人邮箱
 * @param {string} subject - 邮件主题
 * @param {string} content - 邮件内容
 */
export function sendSimpleEmail(to, subject, content) {
    return request({
        url: '/email/simple',
        method: 'post',
        params: {
            to,
            subject,
            content
        }
    })
}

/**
 * 发送HTML邮件
 * @param {string} to - 收件人邮箱
 * @param {string} subject - 邮件主题
 * @param {string} htmlContent - HTML格式的邮件内容
 */
export function sendHtmlEmail(to, subject, htmlContent) {
    return request({
        url: '/email/html',
        method: 'post',
        params: {
            to,
            subject,
            htmlContent
        }
    })
}

/**
 * 使用模板发送邮件
 * @param {string} to - 收件人邮箱
 * @param {string} templateCode - 模板代码
 * @param {Object} variables - 模板变量
 */
export function sendTemplateEmail(to, templateCode, variables) {
    return request({
        url: '/email/template',
        method: 'post',
        params: {
            to,
            templateCode
        },
        data: variables
    })
}

/**
 * 批量发送邮件
 * @param {Array<string>} toList - 收件人邮箱列表
 * @param {string} subject - 邮件主题
 * @param {string} content - 邮件内容
 */
export function batchSendEmail(toList, subject, content) {
    return request({
        url: '/email/batch',
        method: 'post',
        params: {
            subject,
            content
        },
        data: toList
    })
}

/**
 * 获取邮件模板
 * @param {string} templateCode - 模板代码
 */
export function getEmailTemplate(templateCode) {
    return request({
        url: `/email/template/${templateCode}`,
        method: 'get'
    })
}

/**
 * 获取邮件日志
 * @param {number} id - 日志ID
 */
export function getEmailLog(id) {
    return request({
        url: `/email/log/${id}`,
        method: 'get'
    })
} 