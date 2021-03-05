package com.offcn.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.netflix.discovery.converters.Auto;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.contants.ProjectContant;
import com.offcn.project.enums.ProjectImageTypeEnume;
import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Service
public class ProjectCreateServiceImpl implements ProjectCreateService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TProjectMapper projectMapper;
    @Autowired
    private TProjectImagesMapper projectImagesMapper;
    @Autowired
    private TProjectTypeMapper projectTypeMapper;
    @Autowired
    private TProjectTagMapper projectTagMapper;
    @Autowired
    private TReturnMapper returnMapper;

    @Override
    public String initCreateProject(Integer memberId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        ProjectRedisStorageVo projectRedisStorageVo = new ProjectRedisStorageVo();
        //存令牌
        //存memberId
        projectRedisStorageVo.setMemberid(memberId);
        projectRedisStorageVo.setProjectToken(token);
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        stringRedisTemplate.opsForValue().set(ProjectContant.TEMP_PROJECT_PREFIX+token,jsonString);



        return token;
    }

    @Override
    public void saveProjectInfo(ProjectStatusEnume auth, ProjectRedisStorageVo projectVo) {
       //1.创建项目,并赋值
        TProject project = new TProject();
        BeanUtils.copyProperties(projectVo,project);
       //设置时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        project.setCreatedate(format);
        //设置项目的状态
        project.setStatus(auth.getCode()+"");
        //保存 到数据库
        projectMapper.insertSelective(project);
        //2拿到项目id
        Integer projectId = project.getId();
        //3.保存图片--头图
        String headerImage = projectVo.getHeaderImage();
        //Integer id, Integer projectid, String imgurl, Byte imgtype
        TProjectImages tProjectImages = new TProjectImages(null, projectId, headerImage, ProjectImageTypeEnume.HEADER.getCode());
        projectImagesMapper.insertSelective(tProjectImages);
        //--详图
        List<String> detailsImage = projectVo.getDetailsImage();
        if(!CollectionUtils.isEmpty(detailsImage)){
            for (String s : detailsImage) {
                TProjectImages detail = new TProjectImages(null, projectId, s, ProjectImageTypeEnume.DETAILS.getCode());
                projectImagesMapper.insertSelective(detail);
            }
        }
        //4.保存标签
        List<Integer> tagids = projectVo.getTagids();
        if(!CollectionUtils.isEmpty(tagids)){
            for (Integer tagid : tagids) {
                //TProjectTag(Integer id, Integer projectid, Integer tagid)
                TProjectTag tProjectTag = new TProjectTag(null, projectId, tagid);
                projectTagMapper.insertSelective(tProjectTag);
            }
        }
        //5保存分类
        List<Integer> typeids = projectVo.getTypeids();
        if(!CollectionUtils.isEmpty(typeids)){
            for (Integer typeid : typeids) {
                //TProjectType(Integer id, Integer projectid, Integer typeid)
                TProjectType tProjectType = new TProjectType(null, projectId, typeid);
                projectTypeMapper.insertSelective(tProjectType);
            }
        }
        //6.保存回报
        List<TReturn> projectReturns = projectVo.getProjectReturns();
        if(!CollectionUtils.isEmpty(projectReturns)){
            for (TReturn tReturn : projectReturns) {
               tReturn.setProjectid(projectId);
               returnMapper.insertSelective(tReturn);
            }
        }
        //7.清空redis
        stringRedisTemplate.delete(ProjectContant.TEMP_PROJECT_PREFIX+projectVo.getProjectToken());

    }
}
