package work.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.RandomUtilSendMsg;
import com.pub.redis.util.RedisCache;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import rabb.workjob.entity.OnlineUserDo;
import work.service.impl.OnlineUserServiceImpl;
import work.util.SendSmsTx;

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

    @Value("${isTest}")
    private  Integer isTest ;

    @Autowired
    private SendSmsTx sendSmsTx;

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
    @TimingLog
    @RequestMapping(value = "/indentityPerson", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult indentityPerson(@RequestBody OnlineUserDo onlineUserDo){
        try{
            /**
             * 校验短信验证码
             */
            String phone = onlineUserDo.getPhone();
            String phoneCode = onlineUserDo.getPhoneCode();
            String stringCache = redisCache.getStringCache(phone);
            if(StringUtils.isNotBlank(stringCache)&&stringCache.equals(phoneCode)){
                onlineUserServiceImpl.indentityPerson(onlineUserDo);
                return AjaxResult.success();
            }else{
                return AjaxResult.error("验证码错误！");
            }
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 公司实名认证
     */
    @TimingLog
    @RequestMapping(value = "/indentityCompany", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult indentityCompany(@RequestBody OnlineUserDo onlineUserDo){
        try{
            /**
             * 校验短信验证码
             */
            String phone = onlineUserDo.getPhone();
            String phoneCode = onlineUserDo.getPhoneCode();
            String stringCache = redisCache.getStringCache(phone);
            if(StringUtils.isNotBlank(stringCache)&&stringCache.equals(phoneCode)){
                onlineUserServiceImpl.indentityCompany(onlineUserDo);
                return AjaxResult.success();
            }else{
                return AjaxResult.error("验证码错误！");
            }

        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取验证码,校验用户是否已被拉黑
     */
    @TimingLog
    @RequestMapping(value = "/getMsg/{phone}", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getMsg(@PathVariable String phone){
        JSONObject rtn=new JSONObject();
        try {
            if(isTest==0){
                //测试环境
                redisCache.putCacheWithExpireTime(phone,"1111",5*60);
                return AjaxResult.success();
            }else{
                /**
                 * 六位数验证码
                 */
                String sixBitRandom = RandomUtilSendMsg.getSixBitRandom();
                SendSmsResponse sendSmsResponse = sendSmsTx.sendMsg(phone, sixBitRandom);
                SendStatus[] sendStatusSet = sendSmsResponse.getSendStatusSet();
                if(sendStatusSet!=null&&sendStatusSet.length>0){
                    redisCache.putCacheWithExpireTime(phone,String.valueOf(sixBitRandom),5*60);
                    return AjaxResult.success();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return AjaxResult.error("失败，请联系管理员！");
    }

    /**
     * 获取个人信息
     */
    @TimingLog
    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getUser(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            OnlineUserDo byId = onlineUserServiceImpl.getById(id);
            return AjaxResult.success(byId);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

}

