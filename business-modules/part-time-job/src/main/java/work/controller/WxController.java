package work.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;

import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.DateUtils;
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
import org.springframework.web.multipart.MultipartFile;
import rabb.workjob.dto.WorkConstantDto;
import rabb.workjob.entity.OnlineUserDo;
import work.config.FilePathOnlineConfig;
import work.config.WxPayConfig;
import work.service.impl.OnlineUserServiceImpl;
import work.service.impl.SysDataDictionaryServiceImpl;

import javax.annotation.Resource;

import java.io.File;
import java.util.Date;

@RestController
@RequestMapping("/onlinework/wx")
@RefreshScope
public class WxController extends BaseController {

    private Logger log= LoggerFactory.getLogger("wxLogger");
    private Logger callBackLog= LoggerFactory.getLogger("callBackLog");
    private Logger loginLog= LoggerFactory.getLogger("loginLog");

    @Value("${short_token_redis_cache_time}")
    private  Long short_token_redis_cache_time ;

    @Autowired
    private RedisCache redisCache;
    //微信Scope，固定snsapi_login
    private String wxScope = "snsapi_login";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OnlineUserServiceImpl onlineUserServiceImpl;

    @Resource
    private TokenProvider tokenProvider;

    @Autowired
    private SysDataDictionaryServiceImpl sysDataDictionaryServiceImpl;

    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private FilePathOnlineConfig filePathOnlineConfig;



    /**
     * 扫码成功回调
     */
    @RequestMapping(value = "/wxCallback", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult wxCallback(@RequestParam String code,@RequestParam(required = false)String invitationOpenid)  {
        loginLog.info("登录的opendId={},邀请码={}",code,invitationOpenid);
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
        QueryWrapper<OnlineUserDo> wq_user=new QueryWrapper<>();
        wq_user.eq("openid",openid);
        OnlineUserDo userDo = onlineUserServiceImpl.getOne(wq_user);
        String token=null;
        if(userDo==null){
            userDo=new OnlineUserDo();
            userDo.setOpenid(openid);
            userDo.setWxunionid(sesssoin_key);
            userDo.setIsDelete(WorkConstantDto.DeleteStatus.no);
            /**
             * 未实名认证
             */
            userDo.setRoleId(WorkConstantDto.RoleType.no);
            userDo.setCreateTime(new Date());
            userDo.setIdenttityStatus(WorkConstantDto.IdenttityStatus.initialize);
            /**
             * 未绑定手机
             */
            userDo.setPhoneAble(WorkConstantDto.phoneAble.no);
            onlineUserServiceImpl.save(userDo);
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

    public String createToken(OnlineUserDo userDo){
        User mUser=new User();
        mUser.setId(userDo.getId());
        mUser.setPassword(userDo.getOpenid());
        String jwt ="BearerWorkOnline" +  tokenProvider.createTokenNewSchool(mUser);
        redisCache.putCacheWithExpireTime(jwt,mUser,short_token_redis_cache_time);
        return jwt;
    }


    /**
     * 上传文件
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult uploadImage(MultipartFile file){
        try{
            String url= uploadImageFile(file);
            return AjaxResult.success(url);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    public String uploadImageFile(MultipartFile file) throws Exception {
        //存储文件路径
        String root = filePathOnlineConfig.getRoot();
        String baseUrl = filePathOnlineConfig.getBaseUrl();
        String originalFilename = DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS,new Date())+file.getOriginalFilename();
        File localFile = new File(root +"/"+ originalFilename);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        file.transferTo(localFile);
        if (file.isEmpty()) {
            file.getInputStream().close();
        }
        return baseUrl+"/"+originalFilename;
    }





}
