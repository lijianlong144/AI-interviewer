<template>
  <div class="interview-room">
    <div class="room-header">
      <div class="room-info">
        <h2>面试房间: {{ roomCode }}</h2>
        <el-tag v-if="roomStatus === 'ACTIVE'" type="success">进行中</el-tag>
        <el-tag v-else-if="roomStatus === 'WAITING'" type="info">等待中</el-tag>
        <el-tag v-else-if="roomStatus === 'COMPLETED'" type="warning">已结束</el-tag>
      </div>
      <div class="timer">
        <span>总时长: {{ formatTime(totalTime) }}</span>
        <span>当前问题: {{ formatTime(questionTime) }}</span>
      </div>
    </div>

    <el-row :gutter="20" class="room-content">
      <!-- 左侧面板：问题和回答区域 -->
      <el-col :span="16">
        <el-card class="question-card">
          <template #header>
            <div class="card-header">
              <span>当前问题 ({{ currentQuestionIndex + 1 }}/{{ totalQuestions }})</span>
              <div>
                <el-button v-if="hasHint" type="info" plain size="small" @click="showHint">
                  <el-icon><QuestionFilled /></el-icon> 提示
                </el-button>
              </div>
            </div>
          </template>
          <div v-if="currentQuestion" class="question-content">
            <h3>{{ currentQuestion.content }}</h3>
            <div v-if="currentQuestion.description" class="question-description">
              {{ currentQuestion.description }}
            </div>
          </div>
          <el-empty v-else description="暂无问题"></el-empty>
        </el-card>

        <el-card class="answer-card">
          <template #header>
            <div class="card-header">
              <span>您的回答</span>
              <div v-if="isAnswering">
                <el-button type="primary" @click="submitAnswer" :disabled="!answerContent">提交回答</el-button>
              </div>
            </div>
          </template>
          <div v-if="isAnswering" class="answer-input">
            <el-input
              v-model="answerContent"
              type="textarea"
              :rows="8"
              placeholder="请输入您的回答..."
              :maxlength="2000"
              show-word-limit
            ></el-input>
          </div>
          <div v-else-if="lastAnswer" class="answer-preview">
            <div class="answer-text">{{ lastAnswer.content }}</div>
            <div v-if="lastAnswer.evaluation" class="answer-evaluation">
              <h4>AI评估:</h4>
              <div class="evaluation-content">{{ lastAnswer.evaluation }}</div>
              <div class="score">
                <span>得分: </span>
                <el-rate v-model="lastAnswer.score" disabled show-score text-color="#ff9900"></el-rate>
              </div>
            </div>
          </div>
          <el-empty v-else description="等待回答"></el-empty>
        </el-card>
        
        <div class="navigation-buttons">
          <el-button v-if="roomStatus === 'ACTIVE'" type="danger" @click="confirmEndInterview">
            结束面试
          </el-button>
          <el-button v-if="roomStatus === 'COMPLETED'" type="primary" @click="viewResult">
            查看结果
          </el-button>
        </div>
      </el-col>

      <!-- 右侧面板：面试官和聊天区域 -->
      <el-col :span="8">
        <el-card class="interviewer-card">
          <template #header>
            <div class="card-header">
              <span>AI面试官</span>
              <el-tag type="success" v-if="connectionStatus">已连接</el-tag>
              <el-tag type="danger" v-else>未连接</el-tag>
            </div>
          </template>
          <div class="interviewer-avatar">
            <el-avatar :size="100" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"></el-avatar>
            <h3>{{ interviewerName }}</h3>
          </div>
        </el-card>

        <el-card class="chat-card">
          <template #header>
            <div class="card-header">
              <span>面试交流</span>
            </div>
          </template>
          <div class="chat-messages" ref="chatContainer">
            <div v-for="(message, index) in messages" :key="index" class="message" :class="message.type">
              <div class="message-sender">{{ message.sender }}</div>
              <div class="message-content">{{ message.content }}</div>
              <div class="message-time">{{ formatMessageTime(message.timestamp) }}</div>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="messageContent"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :disabled="!connectionStatus || roomStatus !== 'ACTIVE'"
            >
              <template #append>
                <el-button @click="sendMessage" :disabled="!connectionStatus || roomStatus !== 'ACTIVE'">
                  发送
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 提示对话框 -->
    <el-dialog v-model="hintDialogVisible" title="面试提示" width="30%">
      <p v-if="currentHint">{{ currentHint }}</p>
      <p v-else>暂无提示可用</p>
    </el-dialog>

    <!-- 结束面试确认对话框 -->
    <el-dialog v-model="endDialogVisible" title="结束面试" width="30%">
      <p>您确定要结束当前面试吗？此操作不可撤销。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="endDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="endInterview" :loading="endLoading">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { getRoomInfo, joinRoom, leaveRoom, getRoomStatus, getRoomMessages, sendMessage as apiSendMessage } from '@/api/room'
import { getInterviewQuestions, getNextQuestion, submitAnswer as apiSubmitAnswer, getAnswerEvaluation, requestAIHint } from '@/api/question'
import { endInterview as apiEndInterview } from '@/api/interview'
import WebSocketClient from '@/utils/websocket'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatContainer = ref(null)

// 房间信息
const roomCode = route.params.roomCode
const roomStatus = ref('WAITING') // WAITING, ACTIVE, COMPLETED
const interviewId = ref(null)
const interviewerName = ref('AI面试官')

// 面试问题相关
const currentQuestion = ref(null)
const currentQuestionIndex = ref(0)
const totalQuestions = ref(0)
const questions = ref([])
const answerContent = ref('')
const lastAnswer = ref(null)
const isAnswering = ref(true)
const hasHint = ref(false)
const currentHint = ref('')

// 提示对话框控制
const hintDialogVisible = ref(false)

// 结束面试对话框
const endDialogVisible = ref(false)
const endLoading = ref(false)

// 计时器
const totalTime = ref(0)
const questionTime = ref(0)
const timerInterval = ref(null)

// WebSocket连接状态
const wsClient = ref(null)
const connectionStatus = ref(false)

// 聊天消息
const messages = ref([])
const messageContent = ref('')

// 计算属性
const userId = computed(() => userStore.userInfo?.id)

// 格式化时间
const formatTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 格式化消息时间
const formatMessageTime = (timestamp) => {
  return dayjs(timestamp).format('HH:mm:ss')
}

// 初始化房间
const initRoom = async () => {
  try {
    // 获取房间信息
    const res = await getRoomInfo(roomCode)
    
    if (!res.data) {
      ElMessage.error('房间不存在')
      router.push('/candidate/dashboard')
      return
    }
    
    interviewId.value = res.data.interviewId
    roomStatus.value = res.data.status
    
    // 如果房间状态为活跃，加入房间
    if (roomStatus.value === 'ACTIVE') {
      await joinRoom(roomCode, userId.value)
      
      // 获取面试问题
      await loadQuestions()
      
      // 启动计时器
      startTimer()
      
      // 初始化WebSocket连接
      initWebSocket()
      
      // 加载历史消息
      loadHistoryMessages()
    } else if (roomStatus.value === 'WAITING') {
      // 轮询房间状态
      pollRoomStatus()
    } else if (roomStatus.value === 'COMPLETED') {
      ElMessage.info('此面试已结束')
    }
  } catch (error) {
    console.error('初始化房间失败:', error)
    ElMessage.error('初始化房间失败')
  }
}

// 轮询房间状态
const pollRoomStatus = () => {
  const statusInterval = setInterval(async () => {
    try {
      const res = await getRoomStatus(roomCode)
      roomStatus.value = res.data.status
      
      if (roomStatus.value === 'ACTIVE') {
        clearInterval(statusInterval)
        
        // 加入房间
        await joinRoom(roomCode, userId.value)
        
        // 获取面试问题
        await loadQuestions()
        
        // 启动计时器
        startTimer()
        
        // 初始化WebSocket连接
        initWebSocket()
        
        // 加载历史消息
        loadHistoryMessages()
        
        ElMessage.success('面试已开始')
      } else if (roomStatus.value === 'COMPLETED') {
        clearInterval(statusInterval)
        ElMessage.info('面试已结束')
      }
    } catch (error) {
      console.error('获取房间状态失败:', error)
    }
  }, 5000) // 每5秒检查一次
  
  // 组件销毁时清除定时器
  onBeforeUnmount(() => {
    clearInterval(statusInterval)
  })
}

// 加载面试问题
const loadQuestions = async () => {
  try {
    const res = await getInterviewQuestions(interviewId.value)
    questions.value = res.data || []
    totalQuestions.value = questions.value.length
    
    if (questions.value.length > 0) {
      currentQuestion.value = questions.value[0]
      currentQuestionIndex.value = 0
      hasHint.value = true
    }
  } catch (error) {
    console.error('加载面试问题失败:', error)
    ElMessage.error('加载面试问题失败')
  }
}

// 获取下一个问题
const getNextQuestionHandler = async () => {
  try {
    const res = await getNextQuestion(interviewId.value)
    
    if (res.data) {
      currentQuestion.value = res.data
      currentQuestionIndex.value++
      answerContent.value = ''
      isAnswering.value = true
      lastAnswer.value = null
      questionTime.value = 0
      hasHint.value = true
      currentHint.value = ''
    } else {
      // 没有更多问题，面试结束
      ElMessage.info('已完成所有问题')
      await apiEndInterview(interviewId.value)
      roomStatus.value = 'COMPLETED'
      stopTimer()
    }
  } catch (error) {
    console.error('获取下一个问题失败:', error)
    ElMessage.error('获取下一个问题失败')
  }
}

// 提交回答
const submitAnswer = async () => {
  if (!answerContent.value.trim()) {
    ElMessage.warning('请输入您的回答')
    return
  }
  
  try {
    const res = await apiSubmitAnswer({
      interviewId: interviewId.value,
      questionId: currentQuestion.value.id,
      content: answerContent.value,
      timeSpent: questionTime.value
    })
    
    isAnswering.value = false
    
    // 保存回答用于显示
    lastAnswer.value = {
      id: res.data.id,
      content: answerContent.value,
      score: 0
    }
    
    // 获取AI评估
    getEvaluation(res.data.id)
  } catch (error) {
    console.error('提交回答失败:', error)
    ElMessage.error('提交回答失败')
  }
}

// 获取AI评估
const getEvaluation = async (answerId) => {
  try {
    const res = await getAnswerEvaluation(answerId)
    
    if (res.data) {
      lastAnswer.value.evaluation = res.data.content
      lastAnswer.value.score = res.data.score
      
      // 如果不是最后一个问题，添加获取下一题的按钮
      if (currentQuestionIndex.value < totalQuestions.value - 1) {
        setTimeout(() => {
          ElMessageBox.confirm(
            '是否继续下一个问题?',
            '提示',
            {
              confirmButtonText: '继续',
              cancelButtonText: '稍后',
              type: 'info'
            }
          )
          .then(() => {
            getNextQuestionHandler()
          })
          .catch(() => {
            // 用户选择稍后，不做操作
          })
        }, 1000)
      } else {
        ElMessage.success('已完成所有问题')
        setTimeout(() => {
          confirmEndInterview()
        }, 2000)
      }
    }
  } catch (error) {
    console.error('获取评估失败:', error)
  }
}

// 显示提示
const showHint = async () => {
  try {
    if (!currentHint.value) {
      const res = await requestAIHint(interviewId.value, currentQuestion.value.id)
      currentHint.value = res.data?.content || '暂无提示可用'
      hasHint.value = false
    }
    
    hintDialogVisible.value = true
  } catch (error) {
    console.error('获取提示失败:', error)
    ElMessage.error('获取提示失败')
  }
}

// 确认结束面试
const confirmEndInterview = () => {
  endDialogVisible.value = true
}

// 结束面试
const endInterview = async () => {
  try {
    endLoading.value = true
    await apiEndInterview(interviewId.value)
    roomStatus.value = 'COMPLETED'
    stopTimer()
    closeWebSocket()
    ElMessage.success('面试已结束')
    endDialogVisible.value = false
  } catch (error) {
    console.error('结束面试失败:', error)
    ElMessage.error('结束面试失败')
  } finally {
    endLoading.value = false
  }
}

// 查看结果
const viewResult = () => {
  router.push(`/candidate/interview-result/${interviewId.value}`)
}

// 初始化WebSocket
const initWebSocket = () => {
  const wsUrl = `ws://localhost:8083/api/ws/room/${roomCode}?userId=${userId.value}&role=CANDIDATE`
  
  wsClient.value = new WebSocketClient(
    wsUrl,
    handleWsMessage,
    handleWsOpen,
    handleWsClose,
    handleWsError
  )
}

// WebSocket消息处理
const handleWsMessage = (data) => {
  if (data.type === 'CHAT') {
    // 添加聊天消息
    messages.value.push({
      type: data.sender === userId.value ? 'outgoing' : 'incoming',
      sender: data.senderName || (data.sender === userId.value ? '我' : '面试官'),
      content: data.content,
      timestamp: data.timestamp || new Date()
    })
    
    // 滚动到底部
    scrollToBottom()
  } else if (data.type === 'SYSTEM') {
    // 系统消息
    messages.value.push({
      type: 'system',
      sender: '系统',
      content: data.content,
      timestamp: data.timestamp || new Date()
    })
    
    // 滚动到底部
    scrollToBottom()
    
    // 处理系统事件
    if (data.event === 'NEXT_QUESTION') {
      getNextQuestionHandler()
    } else if (data.event === 'END_INTERVIEW') {
      roomStatus.value = 'COMPLETED'
      stopTimer()
      ElMessage.info('面试官已结束面试')
    }
  }
}

// WebSocket连接成功
const handleWsOpen = () => {
  connectionStatus.value = true
  
  // 发送加入房间消息
  wsClient.value.send({
    type: 'SYSTEM',
    event: 'JOIN',
    sender: userId.value,
    content: '候选人加入了面试'
  })
}

// WebSocket连接关闭
const handleWsClose = () => {
  connectionStatus.value = false
}

// WebSocket连接错误
const handleWsError = (error) => {
  connectionStatus.value = false
  console.error('WebSocket连接错误:', error)
}

// 关闭WebSocket连接
const closeWebSocket = () => {
  if (wsClient.value) {
    wsClient.value.close()
    wsClient.value = null
  }
}

// 发送消息
const sendMessage = () => {
  if (!messageContent.value.trim() || !connectionStatus.value) {
    return
  }
  
  const message = {
    type: 'CHAT',
    sender: userId.value,
    senderName: userStore.userInfo?.realName || '候选人',
    content: messageContent.value,
    timestamp: new Date()
  }
  
  // 发送到WebSocket
  if (wsClient.value) {
    wsClient.value.send(message)
  }
  
  // 发送到API（备份）
  apiSendMessage({
    roomCode,
    ...message
  }).catch(error => {
    console.error('发送消息到API失败:', error)
  })
  
  // 清空输入框
  messageContent.value = ''
}

// 加载历史消息
const loadHistoryMessages = async () => {
  try {
    const res = await getRoomMessages(roomCode)
    
    if (res.data && res.data.length > 0) {
      const formattedMessages = res.data.map(msg => ({
        type: msg.sender === userId.value ? 'outgoing' : (msg.type === 'SYSTEM' ? 'system' : 'incoming'),
        sender: msg.type === 'SYSTEM' ? '系统' : (msg.sender === userId.value ? '我' : msg.senderName || '面试官'),
        content: msg.content,
        timestamp: msg.timestamp
      }))
      
      messages.value = formattedMessages
      
      // 滚动到底部
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 启动计时器
const startTimer = () => {
  timerInterval.value = setInterval(() => {
    totalTime.value++
    questionTime.value++
  }, 1000)
}

// 停止计时器
const stopTimer = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

// 监听聊天消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
})

// 组件挂载
onMounted(async () => {
  // 如果没有用户信息，先获取用户信息
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfoAction()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      router.push('/login')
      return
    }
  }
  
  await initRoom()
})

// 组件销毁前
onBeforeUnmount(() => {
  // 离开房间
  if (roomStatus.value === 'ACTIVE' && userId.value) {
    leaveRoom(roomCode, userId.value).catch(error => {
      console.error('离开房间失败:', error)
    })
  }
  
  // 关闭WebSocket连接
  closeWebSocket()
  
  // 停止计时器
  stopTimer()
})
</script>

<style scoped>
.interview-room {
  padding: 20px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.room-info {
  display: flex;
  align-items: center;
}

.room-info h2 {
  margin-right: 10px;
  margin-bottom: 0;
}

.timer {
  background-color: #f0f2f5;
  padding: 8px 16px;
  border-radius: 4px;
  display: flex;
  gap: 20px;
}

.room-content {
  margin-bottom: 20px;
}

.question-card,
.answer-card,
.interviewer-card,
.chat-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-content {
  padding: 10px 0;
}

.question-description {
  margin-top: 10px;
  color: #606266;
  line-height: 1.6;
}

.answer-input {
  margin-bottom: 20px;
}

.answer-preview {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
}

.answer-text {
  white-space: pre-wrap;
  line-height: 1.6;
  margin-bottom: 15px;
}

.answer-evaluation {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.evaluation-content {
  margin: 10px 0;
  color: #303133;
  line-height: 1.6;
}

.score {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.interviewer-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.interviewer-avatar h3 {
  margin-top: 10px;
  margin-bottom: 0;
}

.chat-messages {
  height: 250px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 15px;
  max-width: 80%;
}

.message.incoming {
  margin-right: auto;
}

.message.outgoing {
  margin-left: auto;
}

.message.system {
  margin: 10px auto;
  text-align: center;
  max-width: 100%;
}

.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.message-content {
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-word;
}

.incoming .message-content {
  background-color: #fff;
  border: 1px solid #dcdfe6;
}

.outgoing .message-content {
  background-color: #ecf5ff;
  color: #409eff;
}

.system .message-content {
  background-color: #f0f9eb;
  color: #67c23a;
  display: inline-block;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.navigation-buttons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  gap: 20px;
}
</style> 