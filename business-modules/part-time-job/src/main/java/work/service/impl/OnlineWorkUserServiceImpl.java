package work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rabb.workjob.entity.OnlineResumeDo;
import rabb.workjob.entity.OnlineUserDo;
import rabb.workjob.entity.OnlineWorkDo;
import rabb.workjob.entity.OnlineWorkUserDo;
import rabb.workjob.mapper.OnlineWorkUserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户投递信息 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Service
public class OnlineWorkUserServiceImpl extends ServiceImpl<OnlineWorkUserMapper, OnlineWorkUserDo> implements IService<OnlineWorkUserDo> {

    @Autowired
    private OnlineUserServiceImpl onlineUserService;
    @Autowired
    private OnlineResumeServiceImpl onlineResumeServiceImpl;
    @Autowired
    private OnlineWorkServiceImpl onlineWorkServiceImpl;


    public void submitReleaseWork(OnlineWorkUserDo onlineWorkUserDo) throws Exception{
        User currentUser = UserContext.getCurrentUser();
        Integer id = currentUser.getId();
        OnlineUserDo byId = onlineUserService.getById(id);
        QueryWrapper<OnlineResumeDo> wq=new QueryWrapper<>();
        wq.eq("user_id",byId.getId());
        OnlineResumeDo onlineResumeDo = onlineResumeServiceImpl.getOne(wq);
        if(onlineResumeDo==null){
            throw  new BusinessException("请完善个人简历！");
        }
        QueryWrapper<OnlineWorkUserDo> wq_workuser=new QueryWrapper<>();
        wq_workuser.eq("user_id",byId.getId());
        wq_workuser.eq("work_id",onlineWorkUserDo.getWorkId());
        OnlineWorkUserDo one_db = getOne(wq_workuser);
        if(one_db!=null){
            throw  new BusinessException("已投递过该工作，请勿重复投递！");
        }
        onlineWorkUserDo.setCreateTime(new Date());
        onlineWorkUserDo.setUserId(byId.getId());
        onlineWorkUserDo.setUserName(byId.getName());
        onlineWorkUserDo.setPhone(byId.getPhone());
        onlineWorkUserDo.setResumeId(onlineResumeDo.getId());
        save(onlineWorkUserDo);
    }

    public List<OnlineWorkDo> getMySubmitWorkList(JSONObject req) {
        List<OnlineWorkDo> listRtn=new ArrayList<>();
        User currentUser = UserContext.getCurrentUser();
        Integer id = currentUser.getId();
        QueryWrapper<OnlineWorkUserDo> wq_workuser=new QueryWrapper<>();
        wq_workuser.eq("user_id",id);
        wq_workuser.orderByDesc("id");
        BaseController.startPage();
        List<OnlineWorkUserDo> list = list(wq_workuser);
        for (OnlineWorkUserDo onlineWorkUserDo : list) {
            Integer workId = onlineWorkUserDo.getWorkId();
            OnlineWorkDo byId = onlineWorkServiceImpl.getById(workId);
            listRtn.add(byId);
        }
        return listRtn;
    }
}
