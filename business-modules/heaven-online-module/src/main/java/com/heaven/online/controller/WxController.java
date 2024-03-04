package com.heaven.online.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.heaven.online.config.WxPayConfig;
import com.heaven.online.service.OnlineHeavenUserServiceImpl;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.redis.util.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import rabb.heaven.entity.OnlineHeavenUserDo;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/onlineheaven/wx")
@RefreshScope
public class WxController extends BaseController {

    private Logger log= LoggerFactory.getLogger("wxLogger");
    private Logger callBackLog= LoggerFactory.getLogger("callBackLog");
    private Logger loginLog= LoggerFactory.getLogger("loginLog");

    @Value("${short_token_redis_cache_time}")
    private  Long short_token_redis_cache_time ;
    @Value("${pagePath}")
    private  String pagePath ;

    @Autowired
    private RedisCache redisCache;


    //微信Scope，固定snsapi_login
    private String wxScope = "snsapi_login";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private OnlineHeavenUserServiceImpl onlineHeavenUserServiceImpl;


    @Resource
    private TokenProvider tokenProvider;



    /**
     * 扫码成功回调
     */
    @RequestMapping(value = "/wxCallback", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult wxCallback(@RequestParam String code)  {
        loginLog.info("登录的opendId={}",code);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("code",200);
        if (code == null || "".equals(code)) {
            return error("必传参数code为空！");
        }

        //1.通过code获取access_token
        //String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=APPID&secret=SECRET&js_code=CODE&grant_type=authorization_code";
        url = url.replace("APPID", wxPayConfig.getAppid()).replace("SECRET", wxPayConfig.getWxAppSecret()).replace("CODE", code);
        ResponseEntity<String> tokenData = restTemplate.getForEntity(url, String.class);
        String tokenInfoStr = tokenData.getBody();

        JSONObject tokenInfoObject = JSONObject.parseObject(tokenInfoStr);
        log.info("tokenInfoObject:{}", tokenInfoObject);
        String openid = tokenInfoObject.getString("openid");
        if (openid == null || "".equals(tokenInfoObject.getString("openid"))) {
            log.error("用code获取openid失败");
            return  error("用code获取openid失败！");
        }
        String sesssoin_key = tokenInfoObject.getString("sesssoin_key");
        /**
         * {
         * 	"openid": "oVLC_6lTZxSWkoZA9L1povjQiplA",
         * 	"nickname": "等疯",
         * 	"sex": 0,
         * 	"language": "",
         * 	"city": "",
         * 	"province": "",
         * 	"country": "",
         * 	"headimgurl": "https:\/\/thirdwx.qlogo.cn\/mmopen\/vi_32\/VTmtGJfvcbax9LB3OPSMURhn6jJ97zibC8Ihicsp9nEwicFZwsPeqprkJmTfQ1wLaJRck9O1icIiatmVyWpibFbg8G4w\/132",
         * 	"privilege": [],
         * 	"unionid": "omvh-6e_lb4ZVFoqTKpnOspgSZIY"
         * }
         */


        /**
         * 根据openid查询平台用户：
         *  1. 如果没有查询到用户，则将openid与平台用户绑定，或者注册新账户
         *  2. 如果查询到用户，则调用本地登录方法
         */
        Date date=new Date();
        QueryWrapper<OnlineHeavenUserDo> wq_user=new QueryWrapper<>();
        wq_user.eq("openid",openid);
        OnlineHeavenUserDo userDo = onlineHeavenUserServiceImpl.getOne(wq_user);
        String token=null;
        if(userDo==null){
           //说明是新用户，需要绑定手机
            userDo=new OnlineHeavenUserDo();
            userDo.setOpenid(openid);
            userDo.setWxunionid(sesssoin_key);
            userDo.setIsDelete(9);
            userDo.setRoleId(3);
            userDo.setCreateTime(new Date());
            onlineHeavenUserServiceImpl.save(userDo);
            token = createToken(userDo);
        }else{
            Integer isDelete = userDo.getIsDelete();
            if(0==isDelete){
                //被禁用的用户
                jsonObject.put("code",400);
                jsonObject.put("msg","用户被禁用");
                return success(jsonObject);
            }
            //说明是老用户
            if(StringUtils.isNotBlank(sesssoin_key)){
                userDo.setWxunionid(sesssoin_key);
            }
           token = createToken(userDo);
        }
        jsonObject.put("token",token);
        jsonObject.put("user",userDo);
        return success(jsonObject);
    }

    public String createToken(OnlineHeavenUserDo userDo){
        User mUser=new User();
        /*mUser.setLoginName(userDo.getNickName());*/
        mUser.setId(userDo.getId());
        mUser.setPassword(userDo.getOpenid());
        String jwt ="BearerSchool" +  tokenProvider.createTokenNewSchool(mUser);
        redisCache.putCacheWithExpireTime(jwt,mUser,short_token_redis_cache_time);
        return jwt;
    }




}
