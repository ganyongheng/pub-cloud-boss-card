package offline.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<OnlineWorkDo> getOfflineWorkList(OnlineWorkDo onlineWorkDo) {
        String phone = onlineWorkDo.getPhone();
        Integer identtityStatus = onlineWorkDo.getIdenttityStatus();
        QueryWrapper<OnlineWorkDo> wq=new QueryWrapper<>();
        wq.eq("phone",phone);
        wq.eq("identtity_status",identtityStatus);
        BaseController.startPage();
        List<OnlineWorkDo> list = list(wq);
        return list;
    }

    public void identityWorkList(OnlineWorkDo onlineWorkDo) {
        updateById(onlineWorkDo);
    }
}
