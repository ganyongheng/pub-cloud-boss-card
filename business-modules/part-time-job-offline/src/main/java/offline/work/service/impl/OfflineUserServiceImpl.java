package offline.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.pub.core.exception.BusinessException;
import com.pub.core.utils.DateUtils;
import com.pub.redis.util.RedisCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rabb.workjob.entity.OfflineUserDo;
import rabb.workjob.mapper.OfflineUserMapper;
import rabb.workjob.service.IOfflineUserService;

import javax.annotation.Resource;

/**
 * <p>
 * 离线用户 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-16
 */
@Log4j2
@Service
public class OfflineUserServiceImpl extends ServiceImpl<OfflineUserMapper, OfflineUserDo> implements IOfflineUserService {

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
            return null;
        }
        //更换token
        QueryWrapper<OfflineUserDo> qw=new QueryWrapper<>();
        qw.eq("id", muser.getId());
        OfflineUserDo user = getOne(qw);
        if(user==null){
            //不存在,需要重新登录
            log.info("jwt的userid,查询不到用户id，请重新登录  ===实际传参传参{}",bearerToken);
            return null;
        }
        //生成新的token,然后将旧的数据放回
        String jwt = "BearerWorkOffline" + tokenProvider.createTokenNewSchool(muser);
        redisCache.putCacheWithExpireTime(jwt,muser,short_token_redis_cache_time);
        //删掉过期token
        redisCache.deleteCache(bearerToken);
        jsonObject.put("token",jwt);
        jsonObject.put("user",user);
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="workoffline_cache_" + jwt;
        redisCache.putCacheWithExpireTime(online_cache_jwt,jsonObject.toJSONString(),30);
        return jsonObject;

    }

    public JSONObject login(OfflineUserDo req) throws Exception{
        JSONObject js=new JSONObject();
        /**
         * 校验用户名和密码是否正确
         */
        String name = req.getName();
        QueryWrapper<OfflineUserDo> wq=new QueryWrapper<>();
        wq.eq("name",name);
        wq.last("limit 1");
        OfflineUserDo one_db_name = getOne(wq);
        if(one_db_name==null){
            throw new BusinessException("用戶不存在！");
        }
        String pwd = one_db_name.getPwd();
        if(!pwd.equals(req.getPwd())){
            throw new BusinessException("密码错误！");
        }
        /**
         * 生成token
         */
        User mUser=new User();
        Integer id = one_db_name.getId();
        mUser.setId(id);
        String jwt = "BearerWorkOffline" + tokenProvider.createTokenNewSchool(mUser);
        redisCache.putCacheWithExpireTime(jwt,mUser,short_token_redis_cache_time);
        js.put("user",one_db_name);
        js.put("token",jwt);
        log.info(DateUtils.dateTimeNow()+"时间登录了系统{}",JSONObject.toJSONString(req));
        return js;
    }
}
