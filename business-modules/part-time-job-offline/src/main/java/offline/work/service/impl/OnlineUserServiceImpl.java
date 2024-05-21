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
import rabb.workjob.entity.OnlineUserIdentityHistoryDo;
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

    private OnlineUserIdentityHistoryServiceImpl onlineUserIdentityHistoryService;


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

    public void identityOnlineUser(OnlineUserDo onlineUserDo) throws Exception {
        Integer id = onlineUserDo.getId();
        OnlineUserDo byId = getById(id);
        Integer identtityStatus = byId.getIdenttityStatus();
        if(WorkConstantDto.IdenttityStatus.submit!=identtityStatus||WorkConstantDto.IdenttityStatus.submitCompany!=identtityStatus){
            throw new BusinessException("非认证审核状态！");
        }
        QueryWrapper<OnlineUserIdentityHistoryDo> wq_history=new QueryWrapper<>();
        wq_history.eq("user_id",id);
        wq_history.eq("identtity_status",byId.getIdenttityStatus());
        OnlineUserIdentityHistoryDo one = onlineUserIdentityHistoryService.getOne(wq_history);
        Integer identtityStatus_req = onlineUserDo.getIdenttityStatus();
        if(WorkConstantDto.IdenttityStatus.yes==identtityStatus_req){
            //认证通过
            if(one.getRoleId()==WorkConstantDto.RoleType.company){
                String phone = one.getPhone();
                if(StringUtils.isNotBlank(phone)){
                    onlineUserDo.setPhone(phone);
                    onlineUserDo.setPhoneAble(WorkConstantDto.phoneAble.yes);
                }
                onlineUserDo.setBusinessCode(one.getBusinessCode());
                onlineUserDo.setCompanyName(one.getCompanyName());
                onlineUserDo.setBusinessImageUrl(one.getBusinessImageUrl());
                onlineUserDo.setRoleId(WorkConstantDto.RoleType.company);
            }else{
                String phone = one.getPhone();
                if(StringUtils.isNotBlank(phone)){
                    onlineUserDo.setPhone(phone);
                    onlineUserDo.setPhoneAble(WorkConstantDto.phoneAble.yes);
                }
                onlineUserDo.setIdentityImageDownUrl(one.getIdentityImageDownUrl());
                onlineUserDo.setIdentityImageUpUrl(one.getIdentityImageUpUrl());
                onlineUserDo.setIdentityNumber(one.getIdentityNumber());
                onlineUserDo.setIdentityName(one.getIdentityName());
                onlineUserDo.setRoleId(WorkConstantDto.RoleType.person);
            }
        }else if(WorkConstantDto.IdenttityStatus.no==identtityStatus_req){
            one.setIdenttityMsg(onlineUserDo.getIdenttityMsg());
        }else{
            return;
        }
        one.setIdenttityStatus(identtityStatus_req);
        onlineUserIdentityHistoryService.updateById(one);
        updateById(onlineUserDo);
    }
}
