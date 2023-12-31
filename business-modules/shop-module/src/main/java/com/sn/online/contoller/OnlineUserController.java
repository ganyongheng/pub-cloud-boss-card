package com.sn.online.contoller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.AESUtil;

import com.pub.redis.util.RedisCache;
import com.sn.online.config.Decrypt;
import com.sn.online.config.Encrypt;
import com.sn.online.config.dto.OnlineUserRegisterDto;
import com.sn.online.service.impl.OnlineUserServiceImpl;
import com.sn.online.service.impl.SysDataDictionaryServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import rabb.shop.entity.OnlineUserDo;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * <p>
 * 在线用户表 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2023-07-31
 */
@Controller
@RequestMapping("/online/userDo")
public class OnlineUserController extends BaseController {

    @Autowired
    private OnlineUserServiceImpl onlineUserServiceImpl;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private SysDataDictionaryServiceImpl sysDataDictionaryServiceImpl;

    @Encrypt
    @Decrypt
    @TimingLog
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult login(@RequestBody OnlineUserDo req){
        try{
            JSONObject login = onlineUserServiceImpl.login(req);
            return AjaxResult.success(login);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    /*@TimingLog
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult login(@RequestParam String aes){
        try{
            String aes_json = AESUtil.decryptStandingBook(aes);
            OnlineUserDo req=JSONObject.parseObject(aes_json,OnlineUserDo.class);
            JSONObject login = onlineUserServiceImpl.login(req);
            String rtn_aes = AESUtil.encryptStandingBook(login.toJSONString());
            return AjaxResult.success(rtn_aes,"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }*/
    @Decrypt
    @TimingLog
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult register(@RequestBody @Valid OnlineUserRegisterDto req, BindingResult bindingResult){
        try {
            if(bindingResult.hasErrors()){
                return AjaxResult.error(bindingResult.getFieldError().getDefaultMessage());
            }
            onlineUserServiceImpl.register(req);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }


    /**
     * 发送谷歌邮件 ，第一次注册的时候
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult sendEmail(@RequestParam String emailAddress){
        try{
            if(!emailAddress.endsWith("@gmail.com")){
                return AjaxResult.error("Google email format error ！");
            }
            onlineUserServiceImpl.sendEmail(emailAddress);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 发送谷歌邮件 ，忘记密码时候，邮箱验证
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/sendEmailForgetPwd", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult sendEmailForgetPwd(@RequestParam String emailAddress){
        try{
            if(!emailAddress.endsWith("@gmail.com")){
                return AjaxResult.error("Google email format error ！");
            }
            onlineUserServiceImpl.sendEmailForgetPwd(emailAddress);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 发送谷歌邮件  ，添加银行卡的时候
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/sendEmailBank", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult sendEmailBank(){
        try{
            User currentUser = UserContext.getCurrentUser();
            if(currentUser==null){
                return AjaxResult.error("Please log in !");
            }
            Integer id = currentUser.getId();
            OnlineUserDo onlineUserDo = onlineUserServiceImpl.getById(id);
            String emailAddress = onlineUserDo.getName();
            onlineUserServiceImpl.sendEmailBank(emailAddress);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 修改密码
     * @return
     */
    @Decrypt
    @TimingLog
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult changePassword(@RequestBody JSONObject req){
        try{
            onlineUserServiceImpl.changePassword(req);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 刷新token
     * @return
     */
    @Encrypt
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult refreshToken(){
        HttpServletRequest request = getRequest();
        String bearerToken = request.getHeader(TokenProvider.AUTHORIZATION_HEADER_ONLINE);
        if (!bearerToken.startsWith("Bearer ")) {
            return AjaxResult.error(" Informal token does not start with Bearer !");
        }
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="online_Cache_" + bearerToken;
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

    @TimingLog
    @RequestMapping(value = "/testAES", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult testAES(@RequestBody OnlineUserDo req){
        try{
            String rtn_aes = AESUtil.encryptStandingBook(JSONObject.toJSONString(req));
            return AjaxResult.success(rtn_aes,"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    @TimingLog
    @RequestMapping(value = "/getCode", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getCode(){
        try{
            User currentUser = UserContext.getCurrentUser();
            if(currentUser==null){
                return AjaxResult.error("Please log in !");
            }
            OnlineUserDo byId = onlineUserServiceImpl.getById(currentUser.getId());
            return AjaxResult.success(byId.getMyInvitationCode(),"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    @TimingLog
    @RequestMapping(value = "/shareCode", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult shareCode(){
        try{
            User currentUser = UserContext.getCurrentUser();
            if(currentUser==null){
                return AjaxResult.error("Please log in !");
            }
            OnlineUserDo byId = onlineUserServiceImpl.getById(currentUser.getId());
            String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("shareCode", "shareCode");
            sysBaseParam=sysBaseParam.replace("##",byId.getMyInvitationCode());
            return AjaxResult.success(sysBaseParam,"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    @TimingLog
    @RequestMapping(value = "/getBalance", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getBalance(){
        try{
            User currentUser = UserContext.getCurrentUser();
            if(currentUser==null){
                return AjaxResult.error("Please log in !");
            }
            OnlineUserDo byId = onlineUserServiceImpl.getById(currentUser.getId());
            String balance = byId.getBalance();
            return AjaxResult.success(balance,"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    @Decrypt
    @TimingLog
    @RequestMapping(value = "/forgetPwd", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult forgetPwd(@RequestBody JSONObject req){
        try {
               onlineUserServiceImpl.forgetPwd(req);
                return AjaxResult.success();
            }catch (Exception e){
                e.printStackTrace();
                return AjaxResult.error(e.getMessage());
        }
    }

}

