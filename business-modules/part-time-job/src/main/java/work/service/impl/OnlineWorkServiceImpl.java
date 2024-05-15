package work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import rabb.workjob.dto.WorkConstantDto;
import rabb.workjob.entity.OnlineUserDo;
import rabb.workjob.entity.OnlineWorkDo;
import rabb.workjob.mapper.OnlineWorkMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 工作发布信息 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Service
public class OnlineWorkServiceImpl extends ServiceImpl<OnlineWorkMapper, OnlineWorkDo> implements IService<OnlineWorkDo> {

    @Autowired
    private  OnlineUserServiceImpl onlineUserService;

    public void releaseWork(OnlineWorkDo onlineWorkDo) {
        User currentUser = UserContext.getCurrentUser();
        OnlineUserDo byId = onlineUserService.getById(currentUser.getId());
        Date date = new Date();
        onlineWorkDo.setCreateTime(date);
        onlineWorkDo.setUserId(byId.getId());
        onlineWorkDo.setStatus(WorkConstantDto.WorkStatus.yes);
        onlineWorkDo.setIdenttityStatus(WorkConstantDto.WorkIdenttityStatus.submit);
        onlineWorkDo.setIsHot(WorkConstantDto.isHot.yes);
        save(onlineWorkDo);
    }

    public List<OnlineWorkDo> getMyReleaseWorkList(JSONObject req) {
        User currentUser = UserContext.getCurrentUser();
        QueryWrapper<OnlineWorkDo> wq=new QueryWrapper<>();
        wq.eq("user_id",currentUser.getId());
        wq.orderByDesc("id");
        BaseController.startPage();
        List<OnlineWorkDo> list = list(wq);
        return list;
    }

    public List<OnlineWorkDo> getPublicReleaseWorkList(OnlineWorkDo onlineWorkDo) {
        QueryWrapper<OnlineWorkDo> wq=new QueryWrapper<>();
        Integer isHot = onlineWorkDo.getIsHot();
        if(isHot!=null){
            wq.eq("is_hot",isHot);
        }
        String workAddress = onlineWorkDo.getWorkAddress();
        if(StringUtils.isNotBlank(workAddress)){
            wq.likeLeft("work_address",workAddress);
        }
        String salaryType = onlineWorkDo.getSalaryType();
        if(StringUtils.isNotBlank(salaryType)){
            wq.like("salary_type",salaryType);
        }
        String workType = onlineWorkDo.getWorkType();
        if(StringUtils.isNotBlank(workType)){
            wq.like("work_type",workType);
        }
        wq.orderByDesc("id");
        BaseController.startPage();
        List<OnlineWorkDo> list = list(wq);
        return list;
    }

    public void editReleaseWork(OnlineWorkDo onlineWorkDo) {
        updateById(onlineWorkDo);
    }
}
