package com.heaven.online.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.heaven.online.service.OnlineHeavenShopServiceImpl;
import com.heaven.online.service.OnlineHeavenUserServiceImpl;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.redis.util.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rabb.heaven.entity.OnlineHeavenShopDo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 纪念馆 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-03-09
 */
@Controller
@RequestMapping("/heaven/onlineHeavenUserDo")
public class OnlineHeavenUserController extends BaseController {
    @Autowired
    private com.heaven.online.service.OnlineHeavenUserServiceImpl onlineHeavenUserServiceImpl;

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
        String bearerToken = request.getHeader(TokenProvider.AUTHORIZATION_HEADER_HEAVEN);
        if (!bearerToken.startsWith("BearerHeaven")) {
            return AjaxResult.error(" token的格式不正确 !");
        }
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="heaven_cache_" + bearerToken;
        String redis_cache = redisCache.getStringCache(online_cache_jwt);
        if(StringUtils.isNotBlank(redis_cache)){
            JSONObject jsonObject1 = JSONObject.parseObject(redis_cache);
            return AjaxResult.success(jsonObject1);
        }
        JSONObject new_token = onlineHeavenUserServiceImpl.refreshToken(bearerToken);
        if(new_token==null){
            return AjaxResult.error("请重新登录");
        }
        return AjaxResult.success(new_token);
    }

}

