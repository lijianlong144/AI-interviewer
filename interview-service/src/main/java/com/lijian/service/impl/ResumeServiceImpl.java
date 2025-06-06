package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Resume;
import com.lijian.entity.SysUser;
import com.lijian.mapper.ResumeMapper;
import com.lijian.service.ResumeService;
import com.lijian.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 简历管理服务实现类
 */
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Value("${file.upload.path:/uploads/resumes}")
    private String uploadPath;

    @Value("${file.access.path:/files/resumes}")
    private String accessPath;

    @Autowired
    private SysUserService userService;

    @Override
    @Transactional
    public Resume uploadResume(Long candidateId, MultipartFile file) {
        try {
            // 获取用户信息
            SysUser user = userService.getById(candidateId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 创建上传目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File uploadDir = new File(uploadPath + "/" + datePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
            String fullPath = uploadDir.getPath() + File.separator + newFileName;

            // 保存文件
            file.transferTo(new File(fullPath));

            // 创建简历记录
            Resume resume = new Resume();
            resume.setCandidateId(candidateId);
            resume.setResumeType("UPLOAD");
            resume.setResumeName(originalFilename);
            resume.setResumeUrl(accessPath + "/" + datePath + "/" + newFileName);
            resume.setRealName(user.getUsername());
            resume.setPhone(user.getPhone());
            resume.setEmail(user.getEmail());
            resume.setStatus(1);
            resume.setCreateTime(LocalDateTime.now());
            resume.setUpdateTime(LocalDateTime.now());

            // 保存简历信息
            save(resume);
            return resume;
        } catch (IOException e) {
            throw new RuntimeException("简历上传失败", e);
        }
    }

    @Override
    @Transactional
    public Resume createOnlineResume(Resume resume) {
        // 设置简历类型
        resume.setResumeType("ONLINE");
        resume.setStatus(1);
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());

        // 保存简历信息
        save(resume);
        return resume;
    }

    @Override
    public boolean updateResume(Resume resume) {
        resume.setUpdateTime(LocalDateTime.now());
        return updateById(resume);
    }

    @Override
    public List<Resume> getResumesByCandidateId(Long candidateId) {
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getCandidateId, candidateId)
                .eq(Resume::getStatus, 1)
                .orderByDesc(Resume::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public Resume getDefaultResume(Long candidateId) {
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getCandidateId, candidateId)
                .eq(Resume::getStatus, 1)
                .orderByDesc(Resume::getCreateTime)
                .last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public Page<Resume> pageResumes(Page<Resume> page, Long candidateId, String keyword) {
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (candidateId != null) {
            queryWrapper.eq(Resume::getCandidateId, candidateId);
        }
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Resume::getRealName, keyword)
                    .or()
                    .like(Resume::getSkills, keyword)
                    .or()
                    .like(Resume::getCurrentCompany, keyword)
                    .or()
                    .like(Resume::getCurrentPosition, keyword)
            );
        }
        
        queryWrapper.eq(Resume::getStatus, 1)
                .orderByDesc(Resume::getCreateTime);
        
        return page(page, queryWrapper);
    }
}