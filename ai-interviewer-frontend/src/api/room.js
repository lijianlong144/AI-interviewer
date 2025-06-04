import request from '@/utils/request'

/**
 * 获取面试房间信息
 * @param {string} roomCode - 房间编码
 */
export function getRoomInfo(roomCode) {
  return request({
    url: `/room/${roomCode}`,
    method: 'get'
  })
}

/**
 * 候选人加入面试房间
 * @param {string} roomCode - 房间编码
 * @param {number} userId - 用户ID
 */
export function joinRoom(roomCode, userId) {
  return request({
    url: '/room/join',
    method: 'post',
    data: {
      roomCode,
      userId
    }
  })
}

/**
 * 候选人离开面试房间
 * @param {string} roomCode - 房间编码
 * @param {number} userId - 用户ID
 */
export function leaveRoom(roomCode, userId) {
  return request({
    url: '/room/leave',
    method: 'post',
    data: {
      roomCode,
      userId
    }
  })
}

/**
 * 获取房间连接状态
 * @param {string} roomCode - 房间编码
 */
export function getRoomStatus(roomCode) {
  return request({
    url: `/room/status/${roomCode}`,
    method: 'get'
  })
}

/**
 * 发送面试消息
 * @param {object} message - 消息对象
 */
export function sendMessage(message) {
  return request({
    url: '/room/message',
    method: 'post',
    data: message
  })
}

/**
 * 获取面试房间历史消息
 * @param {string} roomCode - 房间编码
 */
export function getRoomMessages(roomCode) {
  return request({
    url: `/room/messages/${roomCode}`,
    method: 'get'
  })
} 