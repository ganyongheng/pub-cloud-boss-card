package com.cn.school.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.cn.school.config.Constant;
import com.cn.school.entity.TripCarDo;
import com.cn.school.entity.UserDo;
import com.cn.school.mapper.UserMapper;
import com.cn.school.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pub.core.common.OnlineConstants;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.CalculateUtil;
import com.pub.core.utils.StringUtils;
import com.pub.redis.util.RedisCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-14
 */
@Log4j2
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements IUserService {

    @Autowired
    private RedisCache redisCache;

    @Resource
    private TokenProvider tokenProvider;

    @Value("${short_token_redis_cache_time}")
    private  Long short_token_redis_cache_time ;

    public JSONObject refreshToken(String bearerToken) {
        JSONObject jsonObject=new JSONObject();
        User muser = redisCache.getCache(bearerToken,User.class);
        if(muser==null){
            //说明需要重新登录
            log.info("redis的token已过期，请重新登录  ===实际传参传参{}",bearerToken);
            return null;
        }
        //更换token
        QueryWrapper<UserDo> qw=new QueryWrapper<>();
        qw.eq("id", muser.getId());
        UserDo user = getOne(qw);
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
        String jwt = "BearerSchool" + tokenProvider.createTokenNewSchool(muser);
        redisCache.putCacheWithExpireTime(jwt,muser,short_token_redis_cache_time);
        //删掉过期token
        redisCache.deleteCache(bearerToken);
        jsonObject.put("token",jwt);
        jsonObject.put("user",user);
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="school_cache_" + jwt;
        redisCache.putCacheWithExpireTime(online_cache_jwt,jsonObject.toJSONString(),30);
        return jsonObject;
    }

    public UserDo checkPhoneExit(String phone) {
        QueryWrapper<UserDo> wq=new QueryWrapper<>();
        wq.eq("phone",phone);
        UserDo one = getOne(wq);
       return one;
    }

    public List<UserDo> getPageList(UserDo req) {
        QueryWrapper<UserDo> wq=new QueryWrapper<>();
        String phone = req.getPhone();
        if(StringUtils.isNotBlank(phone)){
            wq.like("phone",phone);
        }
        String identityName = req.getIdentityName();
        if(StringUtils.isNotBlank(identityName)){
            wq.like("identity_name",identityName);
        }
        Integer isDelete = req.getIsDelete();
        if(isDelete!=null){
            wq.eq("is_delete",isDelete);
        }
        String school = req.getSchool();
        if(StringUtils.isNotBlank(school)){
            wq.like("school",school);
        }
        BaseController.startPage();
        return  list(wq);
    }

    public void addBlack(UserDo req) {
        updateById(req);
    }


    /**
     * 积分加减
     * @param userId
     * @param type  -1减分
     */
    public synchronized void addIntegral(Integer userId, String totalFee, Integer type) throws Exception {
        UserDo byId = getById(userId);
        String integral = byId.getIntegral();
        if(Constant.TicketAddStatus.DEL==type){
            //如果减票,查看余票释放充足
            BigDecimal cal = CalculateUtil.cal(new StringBuilder(integral).append("-").append(totalFee).toString());
            byId.setIntegral(cal+"");
            updateById(byId);

        }else{
            //添加积分
            BigDecimal cal = CalculateUtil.cal(new StringBuilder(integral).append("+").append(totalFee).toString());
            byId.setIntegral(cal+"");
            updateById(byId);
        }
    }

}
