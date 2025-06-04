/**
 * WebSocket客户端封装
 * 用于面试房间实时通信
 */
export default class WebSocketClient {
  /**
   * 构造函数
   * @param {string} url - WebSocket服务器地址
   * @param {function} onMessageCallback - 收到消息回调
   * @param {function} onOpenCallback - 连接建立回调
   * @param {function} onCloseCallback - 连接关闭回调
   * @param {function} onErrorCallback - 连接错误回调
   */
  constructor(url, onMessageCallback, onOpenCallback, onCloseCallback, onErrorCallback) {
    this.url = url
    this.onMessageCallback = onMessageCallback
    this.onOpenCallback = onOpenCallback
    this.onCloseCallback = onCloseCallback
    this.onErrorCallback = onErrorCallback
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000 // 3秒
    this.socket = null
    this.isConnected = false
    this.isConnecting = false
    this.pingInterval = null
    this.connect()
  }

  /**
   * 建立WebSocket连接
   */
  connect() {
    if (this.isConnecting) return
    
    this.isConnecting = true
    
    try {
      this.socket = new WebSocket(this.url)
      
      // 连接打开事件
      this.socket.onopen = (event) => {
        this.isConnected = true
        this.isConnecting = false
        this.reconnectAttempts = 0
        
        // 设置心跳包，保持连接
        this.pingInterval = setInterval(() => {
          this.sendPing()
        }, 30000) // 30秒发送一次心跳
        
        if (this.onOpenCallback) {
          this.onOpenCallback(event)
        }
      }
      
      // 接收消息事件
      this.socket.onmessage = (event) => {
        if (this.onMessageCallback) {
          try {
            const data = JSON.parse(event.data)
            this.onMessageCallback(data)
          } catch (error) {
            console.error('解析WebSocket消息失败:', error)
          }
        }
      }
      
      // 连接关闭事件
      this.socket.onclose = (event) => {
        this.isConnected = false
        this.isConnecting = false
        
        // 清除心跳包定时器
        if (this.pingInterval) {
          clearInterval(this.pingInterval)
          this.pingInterval = null
        }
        
        // 尝试重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          
          setTimeout(() => {
            this.connect()
          }, this.reconnectInterval)
        }
        
        if (this.onCloseCallback) {
          this.onCloseCallback(event)
        }
      }
      
      // 连接错误事件
      this.socket.onerror = (error) => {
        this.isConnecting = false
        
        if (this.onErrorCallback) {
          this.onErrorCallback(error)
        }
      }
    } catch (error) {
      this.isConnecting = false
      console.error('WebSocket连接失败:', error)
      
      if (this.onErrorCallback) {
        this.onErrorCallback(error)
      }
    }
  }

  /**
   * 发送消息
   * @param {object} data - 要发送的数据对象
   * @returns {boolean} - 是否发送成功
   */
  send(data) {
    if (!this.isConnected) {
      console.warn('WebSocket未连接，无法发送消息')
      return false
    }
    
    try {
      const message = JSON.stringify(data)
      this.socket.send(message)
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  }

  /**
   * 发送心跳包
   */
  sendPing() {
    this.send({ type: 'PING' })
  }

  /**
   * 关闭WebSocket连接
   */
  close() {
    if (this.socket) {
      // 清除心跳包定时器
      if (this.pingInterval) {
        clearInterval(this.pingInterval)
        this.pingInterval = null
      }
      
      this.socket.close()
      this.socket = null
      this.isConnected = false
    }
  }

  /**
   * 检查连接状态
   * @returns {boolean} - 是否已连接
   */
  isConnectedStatus() {
    return this.isConnected
  }
} 