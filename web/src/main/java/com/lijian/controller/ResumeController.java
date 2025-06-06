package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.Resume;
import com.lijian.result.Result;
import com.lijian.service.ResumeService;
import com.lijian.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简历管理控制器
 */


@RestController
@RequestMapping("/api/resume")
@Validated
public class ResumeController {



    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeService resumeService;

    /**
     * 上传简历
     */
    @PostMapping("/upload")
    public Result<Resume> uploadResume(@RequestParam("file") MultipartFile file) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Resume resume = resumeService.uploadResume(currentUserId, file);
        return Result.success(resume);
    }

    /**
     * 创建在线简历
     */
    @PostMapping
    public Result<Resume> createOnlineResume(@Valid @RequestBody Resume resume) {
        // 设置当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        resume.setCandidateId(currentUserId);
        
        Resume createdResume = resumeService.createOnlineResume(resume);
        return Result.success(createdResume);
    }

    /**
     * 更新简历
     */
    @PutMapping
    public Result<Boolean> updateResume(@Valid @RequestBody Resume resume) {
        // 验证是否为当前用户的简历
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Resume existingResume = resumeService.getById(resume.getId());
        logger.info("resume", resume);
        logger.info("existingResume.getCandidateId()",existingResume.getCandidateId());
        logger.info("currentUserId",currentUserId);

        if (existingResume == null || !existingResume.getCandidateId().equals(currentUserId)) {
            return Result.failure("无权操作此简历");
//            return Result.fail("无权操作此简历");
        }

//        logger.info(existingResume == null || !existingResume.getCandidateId().equals(currentUserId));

        boolean result = resumeService.updateResume(resume);
        return Result.success(result);
    }

    /**
     * 获取简历详情
     */
    @GetMapping("/{id}")
    public Result<Resume> getResumeDetail(@PathVariable("id") Long id) {
        Resume resume = resumeService.getById(id);
        return Result.success(resume);
    }

    /**
     * 获取当前用户的所有简历
     */
    @GetMapping("/my")
    public Result<List<Resume>> getMyResumes() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<Resume> resumes = resumeService.getResumesByCandidateId(currentUserId);
        return Result.success(resumes);
    }

    /**
     * 获取当前用户的默认简历
     */
    @GetMapping("/my/default")
    public Result<Resume> getMyDefaultResume() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Resume resume = resumeService.getDefaultResume(currentUserId);
        return Result.success(resume);
    }

    /**
     * 获取指定用户的所有简历（管理员接口）
     */
    @GetMapping("/user/{userId}")
    public Result<List<Resume>> getUserResumes(@PathVariable("userId") Long userId) {
        List<Resume> resumes = resumeService.getResumesByCandidateId(userId);
        return Result.success(resumes);
    }

    /**
     * 分页查询简历（管理员接口）
     */
    @GetMapping("/page")
    public Result<Page<Resume>> pageResumes(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                           @RequestParam(value = "size", defaultValue = "10") Integer size,
                                           @RequestParam(value = "candidateId", required = false) Long candidateId,
                                           @RequestParam(value = "keyword", required = false) String keyword) {
        Page<Resume> page = new Page<>(current, size);
        Page<Resume> resultPage = resumeService.pageResumes(page, candidateId, keyword);
        return Result.success(resultPage);
    }
    /**
     * 管理员根据简历id删除简历
     */

    @DeleteMapping("/admin/{id}")
    public Result<Boolean> adminDeleteResume(@PathVariable("id") Long id) {
        // 验证是否为当前用户的简历

        Resume existingResume = resumeService.getById(id);

        // 逻辑删除
        existingResume.setStatus(0);
        boolean result = resumeService.updateById(existingResume);
        return Result.success(result);
    }




    /**
     * 删除简历
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteResume(@PathVariable("id") Long id) {
        // 验证是否为当前用户的简历
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Resume existingResume = resumeService.getById(id);
        
        if (existingResume == null || !existingResume.getCandidateId().equals(currentUserId)) {
            return Result.failure("无权操作此简历");
//            return Result.fail("无权操作此简历");
        }
        
        // 逻辑删除
        existingResume.setStatus(0);
        boolean result = resumeService.updateById(existingResume);
        return Result.success(result);
    }
}