<template>
  <div class="interview-room-container">
    <div class="room-header">
      <div class="room-info">
        <h2>AI面试房间 - {{ roomCode }}</h2>
        <el-tag type="success" v-if="interviewInfo">{{ interviewInfo.position }}</el-tag>
        <el-tag type="info" v-if="interviewInfo">问题 {{ currentQuestionIndex + 1 }}/{{ questionTypes.length }}</el-tag>
      </div>
      <div class="room-actions">
        <el-button type="danger" @click="showExitDialog">退出面试</el-button>
      </div>
    </div>

    <div class="room-content">
      <!-- 聊天区域 -->
      <div class="chat-container">
        <div class="chat-header">
          <h3>面试对话</h3>
          <div class="question-progress">
            <el-progress :percentage="(currentQuestionIndex / questionTypes.length) * 100" :format="progressFormat" />
          </div>
        </div>
        
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-for="(message, index) in chatMessages" :key="index" 
               :class="['message', message.sender === 'ai' ? 'ai-message' : 'user-message']">
            <div class="message-avatar">
              <el-avatar :size="40">
                {{ message.sender === 'ai' ? 'AI' : userInfo?.name?.charAt(0) || '我' }}
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-sender">{{ message.sender === 'ai' ? 'AI面试官' : userInfo?.name || '我' }}</div>
              <div class="message-text" v-if="message.sender === 'user'">{{ message.text }}</div>
              <div class="message-text markdown-content" v-else v-html="renderMarkdown(message.text)"></div>
              <div class="message-time">{{ formatTime(message.time) }}</div>
            </div>
          </div>
          
          <!-- 备用的手动触发按钮 -->
          <div v-if="showManualTrigger" class="manual-trigger-container">
            <el-alert
              title="系统未能自动开始面试"
              type="warning"
              description="请点击下方按钮手动开始面试"
              show-icon
              :closable="false"
            />
            <el-button type="primary" @click="manuallyStartInterview" :loading="isGenerating">
              开始面试
            </el-button>
          </div>
        </div>
        
        <div class="chat-input">
          <div class="thinking-indicator" v-if="isThinking && !isAnswering">
            <el-progress :percentage="thinkingProgress" :show-text="false" :stroke-width="5" status="warning"></el-progress>
            <span class="thinking-text">思考中... {{ Math.floor(thinkDuration) }}秒</span>
            <el-button type="primary" size="small" @click="startAnswering">开始回答</el-button>
          </div>
          
          <div v-else>
            <div class="answer-indicator" v-if="isAnswering">
              <el-progress :percentage="answeringProgress" :show-text="false" :stroke-width="5" status="success"></el-progress>
              <span class="answer-text">回答中... {{ Math.floor(answerDuration) }}秒</span>
            </div>
            
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="3"
              placeholder="请输入您的回答..."
              @keyup.enter.ctrl="sendMessage"
              @focus="startAnsweringIfThinking"
              :disabled="!currentQuestion || isGenerating"
            />
            <div class="input-actions">
              <el-tooltip content="开始思考" v-if="currentQuestion && !isThinking && !isAnswering && !hasAnswered">
                <el-button type="warning" @click="startThinking">
                  开始思考
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="按Ctrl+Enter发送">
                <el-button 
                  type="primary" 
                  @click="sendMessage" 
                  :disabled="!userInput.trim() || !currentQuestion || isGenerating || !isAnswering || hasAnswered"
                >
                  发送回答 <el-icon><Position /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-tooltip content="下一个问题" v-if="hasAnswered || (currentQuestionIndex === 0 && !currentQuestion && !isGenerating)">
                <el-button type="success" @click="nextQuestion" :disabled="isGenerating">
                  {{ currentQuestionIndex === 0 && !currentQuestion ? '开始面试' : '下一个问题' }}
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 退出确认对话框 -->
    <el-dialog
      v-model="exitDialogVisible"
      title="确认退出"
      width="400px"
    >
      <span>确定要退出当前面试吗？退出后面试将结束，且无法恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exitDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmExit">确认退出</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 面试完成弹窗 -->
    <el-dialog
      v-model="interviewCompletedDialogVisible"
      title="面试完成"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="interview-completed-content">
        <el-icon class="completed-icon" :size="64"><CircleCheckFilled /></el-icon>
        <h2>恭喜您，面试已完成!</h2>
        <p>您已成功完成所有面试问题。我们的系统正在分析您的表现，请耐心等待面试结果。</p>
        <p>感谢您参与此次面试，祝您求职顺利！</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="returnToHome" size="large">返回首页</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Position, CircleCheckFilled } from '@element-plus/icons-vue'
import { getInterviewByRoomCode, endInterview as apiEndInterview } from '@/api/interview'
import { saveAnswer, generateQuestionUrl } from '@/api/ai-interview'
import { getUserInfo as getAuthUserInfo } from '@/utils/auth'
import DOMPurify from 'dompurify'

// 简单的Markdown渲染函数
const renderMarkdown = (text) => {
  if (!text) return '';
  try {
    // 处理基本的Markdown格式
    let html = text
      // 处理代码块
      .replace(/```([^`]+)```/g, '<pre><code>$1</code></pre>')
      // 处理行内代码
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      // 处理标题
      .replace(/^### (.*$)/gm, '<h3>$1</h3>')
      .replace(/^## (.*$)/gm, '<h2>$1</h2>')
      .replace(/^# (.*$)/gm, '<h1>$1</h1>')
      // 处理粗体
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      // 处理斜体
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      // 处理链接
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
      // 处理无序列表
      .replace(/^\s*[\-\*]\s+(.*$)/gm, '<li>$1</li>')
      // 处理段落
      .replace(/\n\n/g, '</p><p>')
      // 处理换行
      .replace(/\n/g, '<br>');
    
    // 包装在段落标签中
    html = '<p>' + html + '</p>';
    // 将连续的li元素包装在ul中
    html = html.replace(/(<li>.*?<\/li>)+/g, '<ul>$&</ul>');
    
    // 使用DOMPurify净化HTML，防止XSS攻击
    return DOMPurify.sanitize(html);
  } catch (error) {
    console.error('Markdown渲染错误:', error);
    return text;
  }
}

const route = useRoute()
const router = useRouter()
const roomCode = route.params.roomCode

// 用户信息
const userInfo = ref(null)

// 面试信息
const interviewInfo = ref(null)
const interviewStartTime = ref(null)
const interviewDuration = ref(0)
const timerInterval = ref(null)

// 聊天相关
const chatMessages = ref([])
const userInput = ref('')
const chatMessagesRef = ref(null)

// 问题相关
const questionTypes = [
  { label: '文化/价值观契合度', value: 'Culture / Values Fit' },
  { label: '行为 - 团队合作', value: 'Behavioral – Teamwork' },
  { label: '行为 - 冲突或领导力', value: 'Behavioral – Conflict or Leadership' },
  { label: '技术 - 基础知识', value: 'Technical – Fundamentals' },
  { label: '技术 - 深入探讨', value: 'Technical – Deep Dive' },
  { label: '情境 - 优先级排序', value: 'Situational – Prioritization' },
  { label: '情境 - 范围管理', value: 'Situational – Scope Management' },
  { label: '系统设计/工作样本', value: 'System-Design / Work-Sample' },
  { label: '批判性思维/验证', value: 'Critical-Thinking / Validation' },
  { label: '元认知/反思', value: 'Meta-Cognitive / Reflection' }
]
const currentQuestionIndex = ref(0)
const currentQuestion = ref(null)
const isGenerating = ref(false)
const hasAnswered = ref(false)

// 备用手动触发
const showManualTrigger = ref(false)
const autoStartFailed = ref(false)

// 思考和回答计时
const isThinking = ref(false)
const isAnswering = ref(false)
const thinkStartTime = ref(null)
const answerStartTime = ref(null)
const thinkDuration = ref(0)
const answerDuration = ref(0)
const thinkingTimer = ref(null)
const answeringTimer = ref(null)
const MAX_THINK_TIME = 300 // 最大思考时间，单位秒
const MAX_ANSWER_TIME = 300 // 最大回答时间，单位秒

// 计算思考进度
const thinkingProgress = computed(() => {
  return Math.min((thinkDuration.value / MAX_THINK_TIME) * 100, 100)
})

// 计算回答进度
const answeringProgress = computed(() => {
  return Math.min((answerDuration.value / MAX_ANSWER_TIME) * 100, 100)
})

// 进度格式化
const progressFormat = (percentage) => {
  return `${currentQuestionIndex.value + 1}/${questionTypes.length}`
}

// 对话框
const exitDialogVisible = ref(false)
const interviewCompletedDialogVisible = ref(false)

// 获取面试信息
const fetchInterviewInfo = async () => {
  console.log('开始获取面试信息，房间号:', roomCode)
  try {
    const response = await getInterviewByRoomCode(roomCode)
    console.log('面试信息API响应:', response)
    
    if (response.success && response.data) {
      interviewInfo.value = response.data
      console.log('成功获取面试信息:', interviewInfo.value)
      
      // 添加AI欢迎消息
      chatMessages.value.push({
        sender: 'ai',
        text: `欢迎参加${interviewInfo.value.position}的面试，我是您的AI面试官。我们将按顺序进行${questionTypes.length}个问题的面试。`,
        time: new Date()
      })
      
      // 开始面试计时
      startInterviewTimer()
      
      // 自动生成第一个问题
      console.log('准备生成第一个问题...')
      setTimeout(() => {
        console.log('开始生成第一个问题')
        generateQuestion()
        
        // 如果8秒后仍然没有问题，显示手动触发按钮
        setTimeout(() => {
          if (!currentQuestion.value && !isGenerating.value && !autoStartFailed.value) {
            console.log('自动生成问题失败，显示手动触发按钮')
            showManualTrigger.value = true
            autoStartFailed.value = true
          }
        }, 8000)
      }, 1500)
    } else {
      console.error('获取面试信息失败:', response)
      ElMessage.error('无法获取面试信息，请检查房间码是否正确')
      router.push('/candidate/interviews')
    }
  } catch (error) {
    console.error('获取面试信息失败:', error)
    ElMessage.error('获取面试信息失败，请稍后重试')
    router.push('/candidate/interviews')
  }
}

// 手动开始面试
const manuallyStartInterview = () => {
  console.log('手动开始面试')
  showManualTrigger.value = false
  generateQuestion()
}

// 开始面试计时
const startInterviewTimer = () => {
  interviewStartTime.value = new Date()
  timerInterval.value = setInterval(() => {
    const now = new Date()
    interviewDuration.value = Math.floor((now - interviewStartTime.value) / 1000)
  }, 1000)
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 生成唯一ID
const generateUniqueId = () => {
  // 生成时间戳 + 6位随机数的纯数字ID
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 900000) + 100000; // 生成100000-999999之间的随机数
  return parseInt(`${timestamp}${random}`);
}

// 生成问题
const generateQuestion = async () => {
  console.log('generateQuestion被调用，当前状态:', {
    interviewInfo: interviewInfo.value ? interviewInfo.value.id : 'null',
    currentQuestionIndex: currentQuestionIndex.value,
    isGenerating: isGenerating.value
  })

  if (!interviewInfo.value || !interviewInfo.value.id) {
    console.error('面试信息不完整，无法生成问题')
    ElMessage.warning('面试信息不完整，无法生成问题')
    return
  }
  
  // 防止重复生成
  if (isGenerating.value) {
    console.warn('已经在生成问题中，忽略此次调用')
    return
  }
  
  isGenerating.value = true
  let questionText = ''
  let questionId = null
  let extractedContent = ''
  let typewriterInterval = null
  let typewriterIndex = 0
  let retryCount = 0
  const maxRetries = 3
  
  const tryGenerateQuestion = async () => {
    try {
      // 生成唯一的问题ID
      const uniqueQuestionId = generateUniqueId()
      
      // 判断是否有上下文（是否是第一个问题）
      const hasContext = currentQuestionIndex.value > 0
      
      // 获取当前问题类型
      const questionType = questionTypes[currentQuestionIndex.value].value
      
      console.log(`正在生成问题: 类型=${questionType}, 问题ID=${uniqueQuestionId}, 有上下文=${hasContext}, 面试ID=${interviewInfo.value.id}`)
      
      // 创建EventSource连接
      const url = generateQuestionUrl(interviewInfo.value.id, questionType, uniqueQuestionId, hasContext)
      console.log('EventSource URL:', url)
      
      const eventSource = new EventSource(url)
      console.log('EventSource已创建')
      
      // 添加一个超时处理
      const timeoutId = setTimeout(() => {
        console.error('生成问题超时')
        eventSource.close()
        
        if (retryCount < maxRetries) {
          retryCount++
          console.log(`尝试重新生成问题 (${retryCount}/${maxRetries})`)
          tryGenerateQuestion()
        } else {
          isGenerating.value = false
          ElMessage.error('生成问题失败，请刷新页面重试')
        }
      }, 30000) // 30秒超时
      
      // 监听打开事件
      eventSource.onopen = (event) => {
        console.log('EventSource连接已打开:', event)
      }
      
      // 添加一个新的AI消息，准备接收流式内容
      chatMessages.value.push({
        sender: 'ai',
        text: '',
        rawText: '',
        time: new Date(),
        isGenerating: true,
        displayedContent: ''  // 用于打字机效果
      })
      
      // 监听消息
      eventSource.onmessage = (event) => {
        console.log('收到消息:', event.data)
        const chunk = event.data
        questionText += chunk
        
        // 尝试提取JSON内容
        try {
          // 尝试从消息文本中提取JSON
          const jsonMatch = questionText.match(/\{.*\}/s);
          if (jsonMatch) {
            const jsonContent = JSON.parse(jsonMatch[0]);
            if (jsonContent && jsonContent.questionContent) {
              // 提取questionContent内容
              extractedContent = jsonContent.questionContent;
            }
          }
        } catch (error) {
          // JSON解析失败，继续使用原始文本
          console.log('JSON解析尝试失败，继续累积文本:', error);
        }
        
        // 获取最后一条消息
        const lastMessage = chatMessages.value[chatMessages.value.length - 1];
        if (lastMessage && lastMessage.sender === 'ai' && lastMessage.isGenerating) {
          // 更新原始文本
          lastMessage.rawText = questionText;
          
          // 如果已经提取到了完整的问题内容，则使用打字机效果显示
          if (extractedContent) {
            // 清除之前的打字机定时器
            if (typewriterInterval) {
              clearInterval(typewriterInterval);
            }
            
            // 设置打字机效果
            typewriterIndex = lastMessage.displayedContent ? lastMessage.displayedContent.length : 0;
            typewriterInterval = setInterval(() => {
              if (typewriterIndex < extractedContent.length) {
                lastMessage.displayedContent = extractedContent.substring(0, typewriterIndex + 1);
                lastMessage.text = lastMessage.displayedContent;
                typewriterIndex++;
              } else {
                clearInterval(typewriterInterval);
                typewriterInterval = null;
              }
            }, 30); // 调整速度
          } else {
            // 如果还没提取到完整内容，就显示累积的文本
            lastMessage.text = questionText;
          }
        }
        
        // 滚动到底部
        scrollToBottom()
      }
      
      // 监听完成事件
      eventSource.addEventListener('done', (event) => {
        clearTimeout(timeoutId)
        console.log('收到完成事件:', event.data)
        try {
          const response = JSON.parse(event.data)
          if (response.status === 'success') {
            questionId = response.interview_question_id
            console.log('成功获取到问题ID:', questionId)
            
            // 清除打字机效果
            if (typewriterInterval) {
              clearInterval(typewriterInterval);
              typewriterInterval = null;
            }
            
            // 更新最后一条消息，移除isGenerating标记
            if (chatMessages.value.length > 0) {
              const lastMessage = chatMessages.value[chatMessages.value.length - 1]
              if (lastMessage.sender === 'ai' && lastMessage.isGenerating) {
                lastMessage.isGenerating = false
                
                // 确保显示完整的内容
                if (extractedContent) {
                  lastMessage.text = extractedContent
                  lastMessage.displayedContent = extractedContent
                }
              }
            }
            
            // 尝试从原始文本中提取JSON内容
            let questionContent = extractedContent || '';
            let questionType = questionTypes[currentQuestionIndex.value].value;
            
            try {
              // 尝试从原始消息文本中提取JSON
              const lastMessage = chatMessages.value[chatMessages.value.length - 1];
              const rawText = lastMessage.rawText || lastMessage.text;
              const jsonMatch = rawText.match(/\{.*\}/s);
              
              if (jsonMatch) {
                const jsonContent = JSON.parse(jsonMatch[0]);
                if (jsonContent) {
                  questionContent = jsonContent.questionContent || questionContent;
                  questionType = jsonContent.questionType || questionType;
                }
              }
            } catch (jsonError) {
              console.error('提取问题内容失败:', jsonError);
              // 如果提取失败，使用已有内容
            }
            
            // 设置当前问题
            currentQuestion.value = {
              id: questionId,
              type: questionType,
              content: questionContent || questionText
            }
            
            console.log('问题生成成功:', currentQuestion.value)
            ElMessage.success('问题生成成功')
            
            // 问题生成成功后自动开始计算思考时间
            startThinking()
          } else {
            console.error('问题生成失败:', response)
            ElMessage.error('问题生成失败: ' + (response.message || '未知错误'))
            if (retryCount < maxRetries) {
              retryCount++
              console.log(`尝试重新生成问题 (${retryCount}/${maxRetries})`)
              tryGenerateQuestion()
            }
          }
        } catch (error) {
          console.error('解析完成事件失败:', error, '原始数据:', event.data)
          ElMessage.error('解析完成事件失败')
          if (retryCount < maxRetries) {
            retryCount++
            console.log(`尝试重新生成问题 (${retryCount}/${maxRetries})`)
            tryGenerateQuestion()
          }
        } finally {
          eventSource.close()
          isGenerating.value = false
        }
      })
      
      // 监听错误
      eventSource.addEventListener('error', (event) => {
        clearTimeout(timeoutId)
        console.error('EventSource错误:', event)
        eventSource.close()
        
        if (retryCount < maxRetries) {
          retryCount++
          console.log(`尝试重新生成问题 (${retryCount}/${maxRetries})`)
          setTimeout(() => {
            tryGenerateQuestion()
          }, 1000)
        } else {
          ElMessage.error('生成问题失败，请刷新页面重试')
          isGenerating.value = false
        }
      })
    } catch (error) {
      console.error('生成问题失败:', error)
      if (retryCount < maxRetries) {
        retryCount++
        console.log(`尝试重新生成问题 (${retryCount}/${maxRetries})`)
        setTimeout(() => {
          tryGenerateQuestion()
        }, 1000)
      } else {
        ElMessage.error('生成问题失败，请刷新页面重试')
        isGenerating.value = false
      }
    }
  }
  
  // 开始尝试生成问题
  tryGenerateQuestion()
}

// 如果正在思考，点击输入框时自动开始回答
const startAnsweringIfThinking = () => {
  if (isThinking.value && !isAnswering.value && !hasAnswered.value) {
    startAnswering();
  }
}

// 开始思考
const startThinking = () => {
  if (!currentQuestion.value) return;
  
  // 如果已经在思考或回答，不重复开始
  if (isThinking.value || isAnswering.value) return;
  
  console.log('开始思考');
  isThinking.value = true;
  thinkStartTime.value = new Date();
  
  // 开始计时
  thinkingTimer.value = setInterval(() => {
    const now = new Date();
    thinkDuration.value = (now - thinkStartTime.value) / 1000;
    
    // 如果超过最大思考时间，自动结束思考
    if (thinkDuration.value >= MAX_THINK_TIME) {
      startAnswering();
    }
  }, 100);
}

// 开始回答
const startAnswering = () => {
  // 停止思考计时
  if (thinkingTimer.value) {
    clearInterval(thinkingTimer.value);
    thinkingTimer.value = null;
  }
  
  // 如果已经在回答，不重复开始
  if (isAnswering.value) return;
  
  console.log('开始回答，思考时长:', Math.floor(thinkDuration.value), '秒');
  isThinking.value = false;
  isAnswering.value = true;
  answerStartTime.value = new Date();
  
  // 开始回答计时
  answeringTimer.value = setInterval(() => {
    const now = new Date();
    answerDuration.value = (now - answerStartTime.value) / 1000;
    
    // 如果超过最大回答时间，自动提交
    if (answerDuration.value >= MAX_ANSWER_TIME && userInput.value.trim()) {
      sendMessage();
    }
  }, 100);
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || !currentQuestion.value || !isAnswering.value || hasAnswered.value) return
  
  // 停止回答计时
  if (answeringTimer.value) {
    clearInterval(answeringTimer.value)
    answeringTimer.value = null
  }
  
  const answerContent = userInput.value
  
  // 记录最终的思考和回答时间
  const finalThinkDuration = Math.floor(thinkDuration.value)
  const finalAnswerDuration = Math.floor(answerDuration.value)
  
  // 保存当前问题ID，防止在异步操作过程中被修改
  const currentQuestionId = currentQuestion.value.id
  
  console.log('提交回答:', {
    思考时间: finalThinkDuration + '秒',
    回答时间: finalAnswerDuration + '秒',
    问题ID: currentQuestionId,
    面试ID: interviewInfo.value.id
  })
  
  // 添加用户消息
  chatMessages.value.push({
    sender: 'user',
    text: answerContent,
    time: new Date()
  })
  
  // 清空输入框
  userInput.value = ''
  
  // 滚动到底部
  scrollToBottom()
  
  // 保存回答到后端
  try {
    const data = {
      answerContent: answerContent,
      interviewId: interviewInfo.value.id,
      interviewQuestionId: currentQuestionId,
      thinkDuration: finalThinkDuration,
      answerDuration: finalAnswerDuration
    }
    
    console.log('发送保存回答请求，参数:', data)
    const response = await saveAnswer(data)
    
    if (response.success) {
      ElMessage.success('回答已保存')
      hasAnswered.value = true
      isAnswering.value = false
      
      // 检查是否是最后一个问题，如果是则显示面试完成弹窗
      if (currentQuestionIndex.value >= questionTypes.length - 1) {
        setTimeout(() => {
          showInterviewCompletedDialog()
        }, 1000)
      }
    } else {
      ElMessage.error('保存回答失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('保存回答失败:', error)
    ElMessage.error('保存回答失败，请重试')
  }
}

// 下一个问题
const nextQuestion = () => {
  // 如果已经是最后一个问题，则结束面试
  if (currentQuestionIndex.value >= questionTypes.length - 1) {
    ElMessage.success('已完成所有问题，面试结束')
    showInterviewCompletedDialog()
    return
  }
  
  // 更新问题索引
  currentQuestionIndex.value++
  
  // 重置状态
  currentQuestion.value = null
  hasAnswered.value = false
  isThinking.value = false
  isAnswering.value = false
  thinkDuration.value = 0
  answerDuration.value = 0
  
  // 生成下一个问题
  generateQuestion()
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

// 显示退出对话框
const showExitDialog = () => {
  exitDialogVisible.value = true
}

// 确认退出
const confirmExit = async () => {
  try {
    console.log('确认退出面试，房间号:', roomCode)
    if (roomCode) {
      await apiEndInterview(roomCode)
    } else {
      console.error('退出面试失败: 房间号不存在')
      ElMessage.error('退出面试失败: 房间号不存在')
      return
    }
    
    // 清除计时器
    if (timerInterval.value) {
      clearInterval(timerInterval.value)
    }
    
    if (thinkingTimer.value) {
      clearInterval(thinkingTimer.value)
    }
    
    if (answeringTimer.value) {
      clearInterval(answeringTimer.value)
    }
    
    ElMessage.success('面试已结束')
    router.push('/candidate/interviews')
  } catch (error) {
    console.error('结束面试失败:', error)
    ElMessage.error('结束面试失败，请稍后重试')
  } finally {
    exitDialogVisible.value = false
  }
}

// 显示面试完成弹窗
const showInterviewCompletedDialog = () => {
  interviewCompletedDialogVisible.value = true
}

// 返回首页
const returnToHome = async () => {
  try {
    console.log('返回首页，结束面试，房间号:', roomCode)
    if (roomCode) {
      await apiEndInterview(roomCode)
    } else {
      console.error('结束面试失败: 房间号不存在')
      ElMessage.error('结束面试失败: 房间号不存在')
      router.push('/candidate/interviews')
      return
    }
    
    // 清除计时器
    if (timerInterval.value) {
      clearInterval(timerInterval.value)
    }
    
    if (thinkingTimer.value) {
      clearInterval(thinkingTimer.value)
    }
    
    if (answeringTimer.value) {
      clearInterval(answeringTimer.value)
    }
    
    router.push('/candidate/interviews')
  } catch (error) {
    console.error('结束面试失败:', error)
    ElMessage.error('结束面试失败，请稍后重试')
    router.push('/candidate/interviews')
  }
}

// 生命周期钩子
onMounted(() => {
  console.log('InterviewRoom组件已挂载，房间号:', roomCode)
  
  // 获取用户信息
  const authUserInfo = getAuthUserInfo()
  if (authUserInfo) {
    try {
      userInfo.value = JSON.parse(authUserInfo)
      console.log('用户信息:', userInfo.value)
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  } else {
    console.warn('未找到用户信息')
  }
  
  // 初始化聊天消息
  chatMessages.value = []
  
  // 获取面试信息
  fetchInterviewInfo()
})

onBeforeUnmount(() => {
  // 清除计时器
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
  }
  
  if (thinkingTimer.value) {
    clearInterval(thinkingTimer.value)
  }
  
  if (answeringTimer.value) {
    clearInterval(answeringTimer.value)
  }
})
</script>

<style scoped>
.interview-room-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.room-header {
  padding: 15px 20px;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.room-info h2 {
  margin: 0;
}

.room-content {
  flex: 1;
  display: flex;
  padding: 20px;
  overflow: hidden;
}

.chat-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.chat-header {
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h3 {
  margin: 0;
}

.question-progress {
  width: 200px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
  margin-bottom: 15px;
}

.ai-message {
  align-self: flex-start;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 8px;
}

.ai-message .message-content {
  background-color: #f0f9ff;
}

.user-message .message-content {
  background-color: #ecf5ff;
  text-align: right;
}

.message-sender {
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.message-text {
  word-break: break-word;
  line-height: 1.5;
}

/* Markdown 样式 */
.markdown-content {
  line-height: 1.6;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  margin-top: 1em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.markdown-content p {
  margin: 0.5em 0;
}

.markdown-content ul,
.markdown-content ol {
  padding-left: 1.5em;
  margin: 0.5em 0;
}

.markdown-content li {
  margin: 0.3em 0;
}

.markdown-content code {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 0.2em 0.4em;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
}

.markdown-content pre {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 1em;
  border-radius: 5px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.markdown-content pre code {
  background-color: transparent;
  padding: 0;
}

.markdown-content blockquote {
  border-left: 4px solid #ddd;
  padding-left: 1em;
  margin-left: 0;
  color: #666;
}

.markdown-content table {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

.markdown-content table th,
.markdown-content table td {
  border: 1px solid #ddd;
  padding: 0.5em;
}

.markdown-content table th {
  background-color: #f0f0f0;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.chat-input {
  margin-top: 15px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.thinking-indicator, .answer-indicator {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f8f8f8;
}

.thinking-text, .answer-text {
  display: block;
  margin: 10px 0;
  color: #606266;
  text-align: center;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  gap: 10px;
}

/* 手动触发按钮样式 */
.manual-trigger-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20px auto;
  padding: 20px;
  background-color: #fdf6ec;
  border-radius: 8px;
  width: 80%;
  max-width: 500px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.manual-trigger-container .el-button {
  margin-top: 15px;
  width: 200px;
}

@media (max-width: 768px) {
  .room-content {
    padding: 10px;
  }
  
  .chat-container {
    padding: 10px;
  }
}

.interview-completed-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px 0;
}

.completed-icon {
  color: #67c23a;
  margin-bottom: 20px;
}

.interview-completed-content h2 {
  margin-bottom: 20px;
  color: #303133;
}

.interview-completed-content p {
  margin: 10px 0;
  color: #606266;
  font-size: 16px;
  line-height: 1.6;
}
</style>