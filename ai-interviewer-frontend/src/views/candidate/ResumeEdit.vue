<template>
  <div class="resume-edit-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-page-header @back="goBack">
          <template #content>
            <div class="page-title">{{ isEdit ? '编辑简历' : '创建简历' }}</div>
          </template>
        </el-page-header>
      </el-col>
    </el-row>

    <div class="edit-content" v-loading="loading">
      <el-form 
        ref="resumeFormRef" 
        :model="resumeForm" 
        :rules="rules" 
        label-width="120px"
        class="resume-form"
      >
        <!-- 基本信息 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
            </div>
          </template>
          
          <el-form-item label="简历名称" prop="resumeName">
            <el-input v-model="resumeForm.resumeName" placeholder="请输入简历名称" />
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="resumeForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="resumeForm.gender">
                  <el-radio label="男">男</el-radio>
                  <el-radio label="女">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker 
                  v-model="resumeForm.birthDate" 
                  type="date" 
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="resumeForm.phone" placeholder="请输入手机号码" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电子邮箱" prop="email">
                <el-input v-model="resumeForm.email" placeholder="请输入电子邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="现居住地" prop="currentLocation">
                <el-input v-model="resumeForm.currentLocation" placeholder="请输入现居住地" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 教育背景 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>教育背景</span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="最高学历" prop="highestEducation">
                <el-select v-model="resumeForm.highestEducation" placeholder="请选择最高学历">
                  <el-option label="高中及以下" value="高中及以下" />
                  <el-option label="大专" value="大专" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="毕业院校" prop="graduationSchool">
                <el-input v-model="resumeForm.graduationSchool" placeholder="请输入毕业院校" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="专业" prop="major">
                <el-input v-model="resumeForm.major" placeholder="请输入专业" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="毕业年份" prop="graduationYear">
                <el-date-picker 
                  v-model="resumeForm.graduationYear" 
                  type="year" 
                  placeholder="选择年份"
                  format="YYYY"
                  value-format="YYYY"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 工作经历 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>工作经历</span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="工作年限" prop="workYears">
                <el-input-number 
                  v-model="resumeForm.workYears" 
                  :min="0" 
                  :max="50"
                  placeholder="工作年限"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="当前公司" prop="currentCompany">
                <el-input v-model="resumeForm.currentCompany" placeholder="请输入当前公司" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="当前职位" prop="currentPosition">
            <el-input v-model="resumeForm.currentPosition" placeholder="请输入当前职位" />
          </el-form-item>
          
          <el-form-item label="工作经历" prop="workExperience">
            <el-input 
              v-model="resumeForm.workExperience" 
              type="textarea" 
              :rows="6"
              placeholder="请详细描述您的工作经历，包括公司、职位、工作时间、主要职责等"
            />
          </el-form-item>
        </el-card>

        <!-- 技能特长 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>技能特长</span>
            </div>
          </template>
          
          <el-form-item label="技能特长" prop="skills">
            <el-input 
              v-model="resumeForm.skills" 
              type="textarea" 
              :rows="4"
              placeholder="请输入您的技能特长，如编程语言、工具软件、专业技能等"
            />
          </el-form-item>
          
          <el-form-item label="自我评价" prop="selfEvaluation">
            <el-input 
              v-model="resumeForm.selfEvaluation" 
              type="textarea" 
              :rows="4"
              placeholder="请输入自我评价"
            />
          </el-form-item>
        </el-card>

        <!-- 求职意向 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>求职意向</span>
            </div>
          </template>
          
          <el-form-item label="期望地点" prop="expectedLocation">
            <el-input v-model="resumeForm.expectedLocation" placeholder="请输入期望工作地点" />
          </el-form-item>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="期望薪资" prop="salaryRange">
                <div class="salary-range">
                  <el-input-number 
                    v-model="resumeForm.expectedSalaryMin" 
                    :min="0"
                    placeholder="最低"
                    style="width: 45%"
                  />
                  <span style="margin: 0 10px;">-</span>
                  <el-input-number 
                    v-model="resumeForm.expectedSalaryMax" 
                    :min="0"
                    placeholder="最高"
                    style="width: 45%"
                  />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="到岗时间" prop="availableDate">
                <el-date-picker 
                  v-model="resumeForm.availableDate" 
                  type="date" 
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="saveResume" :loading="saveLoading">
            {{ isEdit ? '保存修改' : '创建简历' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOnlineResume, updateResume, getResumeDetail } from '@/api/resume'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)
const resumeId = route.params.id

// 表单引用
const resumeFormRef = ref()
const loading = ref(false)
const saveLoading = ref(false)

// 表单数据
const resumeForm = reactive({
  resumeName: '',
  realName: '',
  gender: '男',
  birthDate: '',
  phone: '',
  email: '',
  currentLocation: '',
  expectedLocation: '',
  highestEducation: '',
  graduationSchool: '',
  major: '',
  graduationYear: '',
  workYears: 0,
  currentCompany: '',
  currentPosition: '',
  workExperience: '',
  skills: '',
  selfEvaluation: '',
  expectedSalaryMin: null,
  expectedSalaryMax: null,
  availableDate: ''
})

// 表单验证规则
const rules = {
  resumeName: [
    { required: true, message: '请输入简历名称', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 返回
const goBack = () => {
  router.push('/candidate/resume')
}

// 保存简历
const saveResume = async () => {
  try {
    await resumeFormRef.value.validate()
    
    saveLoading.value = true
    
    if (isEdit.value) {
      // 编辑模式
      await updateResume({
        id: resumeId,
        ...resumeForm
      })
      ElMessage.success('简历更新成功')
    } else {
      // 创建模式
      await createOnlineResume(resumeForm)
      ElMessage.success('简历创建成功')
    }
    
    router.push('/candidate/resume')
  } catch (error) {
    console.error('保存简历失败:', error)
    if (error !== false) { // 排除表单验证失败的情况
      ElMessage.error(error.message || '保存简历失败')
    }
  } finally {
    saveLoading.value = false
  }
}

// 加载简历详情（编辑模式）
const loadResumeDetail = async () => {
  if (!isEdit.value) return
  
  try {
    loading.value = true
    const res = await getResumeDetail(resumeId)
    
    if (res.data) {
      // 填充表单数据
      Object.keys(resumeForm).forEach(key => {
        if (res.data[key] !== undefined) {
          resumeForm[key] = res.data[key]
        }
      })
      
      // 处理毕业年份格式
      if (res.data.graduationYear) {
        resumeForm.graduationYear = String(res.data.graduationYear)
      }
    }
  } catch (error) {
    console.error('获取简历详情失败:', error)
    ElMessage.error('获取简历详情失败')
    router.push('/candidate/resume')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadResumeDetail()
  }
})
</script>

<style scoped>
.resume-edit-container {
  padding: 20px 0;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.edit-content {
  margin-top: 20px;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
}

.resume-form {
  padding: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.salary-range {
  display: flex;
  align-items: center;
  width: 100%;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}
</style>