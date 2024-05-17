package offline.work.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.redis.util.RedisCache;
import offline.work.service.impl.OfflineUserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rabb.workjob.entity.OfflineUserDo;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 离线用户 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-16
 */
@Controller
@RequestMapping("/offlinework/offlineUserDo")
public class OfflineUserController  extends BaseController {

    @Autowired
    private OfflineUserServiceImpl offlineUserService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 刷新token
     * @return
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult refreshToken(){
        HttpServletRequest request = getRequest();
        String bearerToken = request.getHeader(TokenProvider.AUTHORIZATION_HEADER_OFFLINE);
        if (!bearerToken.startsWith("BearerWorkOffline")) {
            return AjaxResult.error(" token的格式不正确 !");
        }
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="workoffline_cache_" + bearerToken;
        String redis_cache = redisCache.getStringCache(online_cache_jwt);
        if(StringUtils.isNotBlank(redis_cache)){
            JSONObject jsonObject1 = JSONObject.parseObject(redis_cache);
            return AjaxResult.success(jsonObject1);
        }
        JSONObject new_token = offlineUserService.refreshToken(bearerToken);
        if(new_token==null){
            return AjaxResult.error("请重新登录");
        }
        return AjaxResult.success(new_token);
    }


    @TimingLog
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult login(@RequestBody OfflineUserDo req){
        try{
            JSONObject login = offlineUserService.login(req);
            return AjaxResult.success(login);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

}

