package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简历管理服务接口
 */
public interface ResumeService extends IService<Resume> {

    /**
     * 上传简历
     * @param candidateId 候选人ID
     * @param file 简历文件
     * @return 简历信息
     */
    Resume uploadResume(Long candidateId, MultipartFile file);

    /**
     * 创建在线简历
     * @param resume 简历信息
     * @return 创建后的简历
     */
    Resume createOnlineResume(Resume resume);

    /**
     * 更新简历信息
     * @param resume 简历信息
     * @return 是否成功
     */
    boolean updateResume(Resume resume);

    /**
     * 获取候选人的所有简历
     * @param candidateId 候选人ID
     * @return 简历列表
     */
    List<Resume> getResumesByCandidateId(Long candidateId);

    /**
     * 获取候选人的默认简历
     * @param candidateId 候选人ID
     * @return 默认简历
     */
    Resume getDefaultResume(Long candidateId);

    /**
     * 分页查询简历
     * @param page 分页参数
     * @param candidateId 候选人ID
     * @param keyword 关键词
     * @return 简历分页列表
     */
    Page<Resume> pageResumes(Page<Resume> page, Long candidateId, String keyword);
}