package com.offcn.project.service;

import com.offcn.project.po.*;

import java.util.List;

public interface ProjectInfoSerivce {
    //根据项目的id查询回报列表
    public List<TReturn> getReturnList(Integer projectId);
    List<TProject> findAllProject();

    /**
     * 获取项目图片
     * @param id
     * @return
     */
    List<TProjectImages> getProjectImages(Integer id);
    //根据id查询项目的详情
    TProject findProjectInfo(Integer projectId);
    List<TTag> findAllTag();
    List<TType> findAllType();
    TReturn findReturnInfo(Integer returnId);
}
