package com.offcn.project.service.impl;

import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectInfoSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProjectInfoSerivceImpl implements ProjectInfoSerivce {
    @Autowired
    private TReturnMapper returnMapper;
    @Autowired
    private TProjectMapper projectMapper;

    @Autowired
    private TProjectImagesMapper projectImagesMapper;
    @Autowired
    private TTypeMapper typeMapper;
    @Autowired
    private TTagMapper tTagMapper;

    @Override
    public List<TReturn> getReturnList(Integer projectId) {
        TReturnExample example = new TReturnExample();
        example.createCriteria().andProjectidEqualTo(projectId);

        return returnMapper.selectByExample(example);
    }

    @Override
    public List<TProject> findAllProject() {
        return projectMapper.selectByExample(null);
    }

    @Override
    public List<TProjectImages> getProjectImages(Integer id) {
        TProjectImagesExample example = new TProjectImagesExample();
        example.createCriteria().andProjectidEqualTo(id);
        return projectImagesMapper.selectByExample(example);
    }

    @Override
    public TProject findProjectInfo(Integer projectId) {
        TProject project = projectMapper.selectByPrimaryKey(projectId);
        return project;
    }

    @Override
    public List<TTag> findAllTag() {
        return tTagMapper.selectByExample(null);
    }

    @Override
    public List<TType> findAllType() {
        return typeMapper.selectByExample(null);
    }

    @Override
    public TReturn findReturnInfo(Integer returnId) {
        return returnMapper.selectByPrimaryKey(returnId);
    }
}
