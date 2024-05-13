package work.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.redis.util.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import work.entity.OnlineUserDo;
import work.service.impl.OnlineUserServiceImpl;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Controller
@RequestMapping("/onlinework/onlineUserDo")
public class OnlineUserController extends BaseController {

    @Autowired
    private OnlineUserServiceImpl onlineUserServiceImpl;

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
        String bearerToken = request.getHeader(TokenProvider.AUTHORIZATION_HEADER_ONLINE);
        if (!bearerToken.startsWith("BearerWorkOnline")) {
            return AjaxResult.error(" token的格式不正确 !");
        }
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="workonline_cache_" + bearerToken;
        String redis_cache = redisCache.getStringCache(online_cache_jwt);
        if(StringUtils.isNotBlank(redis_cache)){
            JSONObject jsonObject1 = JSONObject.parseObject(redis_cache);
            return AjaxResult.success(jsonObject1);
        }
        JSONObject new_token = onlineUserServiceImpl.refreshToken(bearerToken);
        if(new_token==null){
            return AjaxResult.error("请重新登录");
        }
        return AjaxResult.success(new_token);
    }

    /**
     * 个人实名认证
     */

    /**
     * 删除航线接口
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/deleteTripAreaDo", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult indentityPerson(@RequestBody OnlineUserDo onlineUserDo){
        try{
            onlineUserServiceImpl.indentityPerson(onlineUserDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 公司实名认证
     */
    @TimingLog
    @RequestMapping(value = "/deleteTripAreaDo", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult indentityCompany(@RequestBody OnlineUserDo onlineUserDo){
        try{
            onlineUserServiceImpl.indentityCompany(onlineUserDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

}

