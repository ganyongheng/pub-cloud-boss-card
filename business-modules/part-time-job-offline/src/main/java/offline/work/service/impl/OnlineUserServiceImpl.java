package offline.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.pub.core.common.OnlineConstants;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.StringUtils;
import com.pub.redis.util.RedisCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import rabb.workjob.dto.WorkConstantDto;
import rabb.workjob.entity.OnlineUserDo;
import rabb.workjob.mapper.OnlineUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Log4j2
@Service
public class OnlineUserServiceImpl extends ServiceImpl<OnlineUserMapper, OnlineUserDo> implements IService<OnlineUserDo> {



    public List<OnlineUserDo> getOfflineOnlineUserList(OnlineUserDo onlineUserDo) {
        Integer identtityStatus = onlineUserDo.getIdenttityStatus();
        Integer roleId = onlineUserDo.getRoleId();
        Integer phoneAble = onlineUserDo.getPhoneAble();
        String phone = onlineUserDo.getPhone();
        QueryWrapper<OnlineUserDo> wq=new QueryWrapper<>();
        if(identtityStatus!=null){
            wq.eq("identtity_status",identtityStatus);
        }

        if(StringUtils.isNotBlank(phone)){
            wq.eq("phone",phone);
        }

        if(phoneAble!=null){
            wq.eq("phone_able",phoneAble);
        }

        if(roleId!=null){
            wq.eq("role_id",roleId);
        }

        BaseController.startPage();
        List<OnlineUserDo> list = list(wq);
        return list;
    }

    public void identityWorkList(OnlineUserDo onlineUserDo) {
        updateById(onlineUserDo);
    }
}
