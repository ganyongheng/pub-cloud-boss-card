package work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.common.OnlineConstants;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.domain.AjaxResult;
import com.pub.redis.util.RedisCache;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import rabb.workjob.dto.WorkConstantDto;
import rabb.workjob.entity.OnlineUserDo;
import rabb.workjob.entity.OnlineUserIdentityHistoryDo;
import rabb.workjob.mapper.OnlineUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
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

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private OnlineUserIdentityHistoryServiceImpl onlineUserIdentityHistoryServiceImpl;

    @Resource
    private TokenProvider tokenProvider;

    @Value("${short_token_redis_cache_time}")
    private  Long short_token_redis_cache_time ;

    public JSONObject refreshToken(String bearerToken) {
        JSONObject jsonObject=new JSONObject();
        User muser = redisCache.getCache(bearerToken,User.class);
        if(muser==null){
            //说明需要重新登录
            return null;
        }
        //更换token
        QueryWrapper<OnlineUserDo> qw=new QueryWrapper<>();
        qw.eq("id", muser.getId());
        OnlineUserDo user = getOne(qw);
        if(user==null){
            //不存在,需要重新登录
            log.info("jwt的userid,查询不到用户id，请重新登录  ===实际传参传参{}",bearerToken);
            return null;
        }
        Integer isBlack = user.getIsDelete();
        if(isBlack!=null&&isBlack== OnlineConstants.deleteStats.delete){
            //不存在,需要重新登录
            log.info("jwt的userid,查询到用户id，用户已被拉黑{}",bearerToken);
            return null;
        }
        //生成新的token,然后将旧的数据放回
        String jwt = "BearerWorkOnline" + tokenProvider.createTokenNewSchool(muser);
        redisCache.putCacheWithExpireTime(jwt,muser,short_token_redis_cache_time);
        //删掉过期token
        redisCache.deleteCache(bearerToken);
        jsonObject.put("token",jwt);
        jsonObject.put("user",user);
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="workonline_cache_" + jwt;
        redisCache.putCacheWithExpireTime(online_cache_jwt,jsonObject.toJSONString(),30);
        return jsonObject;
    }

    public void indentityPerson(OnlineUserDo onlineUserDo) throws  Exception{
        User currentUser = UserContext.getCurrentUser();
        Integer id = currentUser.getId();
        onlineUserDo.setId(id);
        /**
         * 校验身份证是否被使用过
         */
        QueryWrapper<OnlineUserDo> wq_one=new QueryWrapper<>();
        wq_one.eq("identity_number",onlineUserDo.getIdentityNumber());
        wq_one.ne("id",onlineUserDo.getId());
        OnlineUserDo one = getOne(wq_one);
        if(one!=null){
           throw new BusinessException("身份证已被认证！");
        }
        QueryWrapper<OnlineUserIdentityHistoryDo> wq_history=new QueryWrapper<>();
        wq_history.eq("user_id",onlineUserDo.getId());
        wq_history.eq("identtity_status",WorkConstantDto.IdenttityStatus.submitCompany);
        List<OnlineUserIdentityHistoryDo> listOnlineUserIdentityHistoryDo = onlineUserIdentityHistoryServiceImpl.list(wq_history);
        if(listOnlineUserIdentityHistoryDo!=null&& !listOnlineUserIdentityHistoryDo.isEmpty()){
            throw new BusinessException("已提交企业认证审核中！");
        }
        OnlineUserDo onlineUserUpdate=new OnlineUserDo();
        onlineUserUpdate.setId(onlineUserDo.getId());
        onlineUserUpdate.setIdenttityStatus(WorkConstantDto.IdenttityStatus.submit);
        updateById(onlineUserUpdate);
        QueryWrapper<OnlineUserIdentityHistoryDo> wq_delete=new QueryWrapper<>();
        wq_delete.eq("user_id",onlineUserDo.getId());
        wq_delete.eq("identtity_status",WorkConstantDto.IdenttityStatus.submit);
        onlineUserIdentityHistoryServiceImpl.remove(wq_delete);
        OnlineUserIdentityHistoryDo onlineUserIdentityHistoryDo=new OnlineUserIdentityHistoryDo();
        onlineUserIdentityHistoryDo.setIdentityName(onlineUserDo.getIdentityName());
        onlineUserIdentityHistoryDo.setIdentityNumber(onlineUserDo.getIdentityNumber());
        onlineUserIdentityHistoryDo.setPhone(onlineUserDo.getPhone());
        onlineUserIdentityHistoryDo.setIdentityImageDownUrl(onlineUserDo.getIdentityImageDownUrl());
        onlineUserIdentityHistoryDo.setIdentityImageUpUrl(onlineUserDo.getIdentityImageUpUrl());
        onlineUserIdentityHistoryDo.setId(onlineUserDo.getId());
        onlineUserIdentityHistoryDo.setCreateTime(new Date());
        onlineUserIdentityHistoryDo.setUserId(onlineUserDo.getId());
        onlineUserIdentityHistoryDo.setRoleId(WorkConstantDto.RoleType.person);
        onlineUserIdentityHistoryDo.setIdenttityStatus(WorkConstantDto.IdenttityStatus.submit);
        onlineUserIdentityHistoryServiceImpl.save(onlineUserIdentityHistoryDo);
    }

    public void indentityCompany(OnlineUserDo onlineUserDo) throws Exception{
        User currentUser = UserContext.getCurrentUser();
        Integer id = currentUser.getId();
        onlineUserDo.setId(id);
        /**
         * 校验公司是否被注册过
         */
        QueryWrapper<OnlineUserDo> wq_one=new QueryWrapper<>();
        wq_one.eq("business_code",onlineUserDo.getBusinessCode());
        wq_one.ne("id",onlineUserDo.getId());
        OnlineUserDo one = getOne(wq_one);
        if(one!=null){
            throw new BusinessException("企业已被注册过！");
        }
        QueryWrapper<OnlineUserIdentityHistoryDo> wq_history=new QueryWrapper<>();
        wq_history.eq("user_id",onlineUserDo.getId());
        wq_history.eq("identtity_status",WorkConstantDto.IdenttityStatus.submit);
        List<OnlineUserIdentityHistoryDo> listOnlineUserIdentityHistoryDo = onlineUserIdentityHistoryServiceImpl.list(wq_history);
        if(listOnlineUserIdentityHistoryDo!=null&& !listOnlineUserIdentityHistoryDo.isEmpty()){
            throw new BusinessException("已提交个人认证审核中！");
        }
        OnlineUserDo onlineUserUpdate=new OnlineUserDo();
        onlineUserUpdate.setId(onlineUserDo.getId());
        onlineUserUpdate.setIdenttityStatus(WorkConstantDto.IdenttityStatus.submitCompany);
        updateById(onlineUserUpdate);
        QueryWrapper<OnlineUserIdentityHistoryDo> wq_delete=new QueryWrapper<>();
        wq_delete.eq("user_id",onlineUserDo.getId());
        wq_delete.eq("identtity_status",WorkConstantDto.IdenttityStatus.submitCompany);
        onlineUserIdentityHistoryServiceImpl.remove(wq_delete);
        OnlineUserIdentityHistoryDo onlineUserIdentityHistoryDo=new OnlineUserIdentityHistoryDo();
        onlineUserIdentityHistoryDo.setBusinessCode(onlineUserDo.getBusinessCode());
        onlineUserIdentityHistoryDo.setBusinessImageUrl(onlineUserDo.getBusinessImageUrl());
        onlineUserIdentityHistoryDo.setCompanyName(onlineUserDo.getCompanyName());
        onlineUserIdentityHistoryDo.setId(onlineUserDo.getId());
        onlineUserIdentityHistoryDo.setCreateTime(new Date());
        onlineUserIdentityHistoryDo.setPhone(onlineUserDo.getPhone());
        onlineUserIdentityHistoryDo.setRoleId(WorkConstantDto.RoleType.company);
        onlineUserIdentityHistoryDo.setIdenttityStatus(WorkConstantDto.IdenttityStatus.submitCompany);
        onlineUserIdentityHistoryServiceImpl.save(onlineUserIdentityHistoryDo);
    }

    public void updatePhoneAble(OnlineUserDo onlineUserDo) throws Exception{
        User currentUser = UserContext.getCurrentUser();
        Integer id = currentUser.getId();
        QueryWrapper<OnlineUserDo> wq_one=new QueryWrapper<>();
        wq_one.eq("phone",onlineUserDo.getPhone());
        wq_one.ne("id",id);
        OnlineUserDo one = getOne(wq_one);
        if(one!=null){
            throw new BusinessException("手机号已被绑定！");
        }
        onlineUserDo.setId(id);
        onlineUserDo.setPhoneAble(WorkConstantDto.phoneAble.yes);
        updateById(onlineUserDo);
    }
}
