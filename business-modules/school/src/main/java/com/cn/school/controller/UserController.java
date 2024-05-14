package com.cn.school.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.cn.school.config.Constant;
import com.cn.school.entity.TripOrderDo;
import com.cn.school.entity.UserDo;
import com.cn.school.service.impl.TripOrderServiceImpl;
import com.cn.school.service.impl.UserServiceImpl;
import com.cn.school.util.SendSmsTx;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.util.page.TableDataInfo;
import com.pub.core.utils.CalculateUtil;
import com.pub.core.utils.DateUtils;
import com.pub.core.utils.RandomUtilSendMsg;
import com.pub.redis.util.RedisCache;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-14
 */
@Controller
@RequestMapping("/school/userDo")
public class UserController extends BaseController {

    @Autowired
    private UserServiceImpl userService;


    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SendSmsTx sendSmsTx;
    @Autowired
    private TripOrderServiceImpl  tripOrderServiceImpl;

    /**
     * 0是测试  1是正式myInvitationUser
     */
    @Value("${isTest}")
    private  Integer isTest ;
    /**
     * 刷新token
     * @return
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult refreshToken(){
        HttpServletRequest request = getRequest();
        String bearerToken = request.getHeader(TokenProvider.AUTHORIZATION_HEADER_ONLINE);
        if (!bearerToken.startsWith("BearerSchool")) {
            return AjaxResult.error(" token的格式不正确 !");
        }
        //##缓存一段时间,避免高并发重复请求,因为会出现并发请求拿新的token来换token情况,缓存30秒
        String online_cache_jwt="school_cache_" + bearerToken;
        String redis_cache = redisCache.getStringCache(online_cache_jwt);
        if(StringUtils.isNotBlank(redis_cache)){
            JSONObject jsonObject1 = JSONObject.parseObject(redis_cache);
            return AjaxResult.success(jsonObject1);
        }
        JSONObject new_token = userService.refreshToken(bearerToken);
        if(new_token==null){
            return AjaxResult.error("请重新登录");
        }
        return AjaxResult.success(new_token);
    }

    /**
     * 第一次下单是否实名认证接口
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/checkIdentity", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult checkIdentity(){
        try{
            JSONObject rtn=new JSONObject();
            rtn.put("code",1);
            rtn.put("msg","已认证");
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byIUserDod = userService.getById(id);
            String phone = byIUserDod.getPhone();
            if(com.pub.core.utils.StringUtils.isBlank(phone)){
                rtn.put("code",0);
                rtn.put("msg","请实名认证");
            }
            return AjaxResult.success(rtn);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 第一次下单实名认证接口提交信息
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/addIdentity", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult addIdentity(@RequestBody UserDo userDo){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            String phoneCode = userDo.getPhoneCode();
            String phone = userDo.getPhone();
            String stringCache = redisCache.getStringCache(phone);
            if(!phoneCode.equals(stringCache)){
                return AjaxResult.error("手机验证码错误");
            }
            UserDo byIUserDod = userService.getById(id);

            byIUserDod.setPhone(phone);
            byIUserDod.setSchool(userDo.getSchool());
            byIUserDod.setIdentityName(userDo.getIdentityName());
            userService.updateById(byIUserDod);
            return AjaxResult.success();
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
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            //第一步校验这个手机号是否被使用
            UserDo userDo = userService.checkPhoneExit(phone,id);
            if(userDo!=null){
                return AjaxResult.error("手机号已被注册过！");
            }

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
     * 分页数据
     * @param req
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/getPageList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getPageList(@RequestBody UserDo req){
        try{
            List<UserDo> pageList = userService.getPageList(req);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 拉黑接口
     * @param req
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/addBlack", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult addBlack(@RequestBody UserDo req){
        try{
            userService.addBlack(req);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 修改角色  1 系统管理员 2 扫描人员 3普通用户
     * @param req
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/addRole", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult addRole(@RequestBody UserDo req){
        try{
            userService.updateById(req);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 获取邀请人列表
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/myInvitationUser", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult myInvitationUser(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            QueryWrapper<UserDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            wq.orderByDesc("create_time");
            List<UserDo> list = userService.list(wq);
            for (UserDo userDo : list) {
                userDo.setNoticeStatus(1);
                userService.updateById(userDo);
            }
            return AjaxResult.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 修改用户积分
     * @param req
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/addIntegral", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult addIntegral(@RequestBody UserDo req){
        try{
            userService.updateById(req);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 获取用户积分
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/getIntegral", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getIntegral(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            String integral = byId.getIntegral();
            return AjaxResult.success("成功",integral);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    /**
     * 第一次下单实名认证接口提交信息
     * @return
     */
    @TimingLog
    @RequestMapping(value = "updateIdentity", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult updateIdentity(@RequestBody UserDo userDo){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            String phoneCode = userDo.getPhoneCode();
            String phone = userDo.getPhone();
            String stringCache = redisCache.getStringCache(phone);
            if(!phoneCode.equals(stringCache)){
                return AjaxResult.error("手机验证码错误");
            }
            UserDo byIUserDod = userService.getById(id);
            if(StringUtils.isNotBlank(phone)){
                byIUserDod.setPhone(phone);
            }
            byIUserDod.setSchool(userDo.getSchool());
            byIUserDod.setIdentityName(userDo.getIdentityName());
            userService.updateById(byIUserDod);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 获取今日预估收益 本月 上月预估收益
     */

    @TimingLog
    @RequestMapping(value = "/getTotalFee", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getTotalFee(){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("day","0");
        jsonObject.put("month","0");
        jsonObject.put("lastmonth","0");
        try{

            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byIUserDod = userService.getById(id);
            String openid = byIUserDod.getOpenid();
            jsonObject.put("integral",byIUserDod.getIntegral());
            //今日预估
            BigDecimal day = getTimeTotal(openid, DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, new Date()));
            if(day!=null){
                jsonObject.put("day",day);
            }
            //本月预估
            BigDecimal month = getTimeTotal(openid, DateUtils.parseDateToStr(DateUtils.YYYY_MM, new Date()));
            if(month!=null){
                jsonObject.put("month",month);
            }
            //上月预估
            BigDecimal lastmonth = getTimeTotal(openid, DateUtils.parseDateToStr(DateUtils.YYYY_MM, DateUtils.addMonths(new Date(), -1)));
            if(lastmonth!=null){
                jsonObject.put("lastmonth",lastmonth);
            }
            return AjaxResult.success(jsonObject);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    public  BigDecimal getTimeTotal(String openid,String time ){
        QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
        wq.eq("invitation_openid",openid);
        wq.like("create_time", time);
        wq.eq("status", Constant.InvitationStatus.SUCESS);
        List<TripOrderDo> list = tripOrderServiceImpl.list(wq);
        if(list!=null&&list.size()>0){
            //计算今日预估积分费用
            StringBuilder sb=new StringBuilder();
            for (TripOrderDo tripOrderDo : list) {
                String invitationFee = tripOrderDo.getInvitationFee();
                sb.append(invitationFee).append("+");
            }
            if(sb.toString().endsWith("+")){
                BigDecimal cal = CalculateUtil.cal(sb.append("0").toString());
                return cal;
            }
        }
        return null;
    }

    /**
     * 获取邀请人列表
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/myInvitationNewUserCount", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult myInvitationNewUserCount(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            QueryWrapper<UserDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            wq.eq("notice_status",0);
            List<UserDo> list = userService.list(wq);
            if(list!=null){
                return AjaxResult.success(list.size());
            }else{
                return AjaxResult.success(0);
            }

        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 获取新邀请人列表
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/myInvitationNewUser", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult myInvitationNewUser(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            QueryWrapper<UserDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            wq.eq("notice_status",0);
            List<UserDo> list = userService.list(wq);
            for (UserDo userDo : list) {
                userDo.setNoticeStatus(1);
                userService.updateById(userDo);
            }
            return AjaxResult.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    /**
     * 获取邀请人列表总数
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/myInvitationUserCount", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult myInvitationUserCount(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            QueryWrapper<UserDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            long count = userService.count(wq);
            return AjaxResult.success(count);

        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 编辑个人信息
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/editPersonInfo", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult editPersonInfo(@RequestBody UserDo userDo){
        try{
            String phone = userDo.getPhone();
            String phoneCode = userDo.getPhoneCode();
            String stringCache = redisCache.getStringCache(phone);
            if(!phoneCode.equals(stringCache)){
                return AjaxResult.error("手机验证码错误");
            }
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            userDo.setId(byId.getId());
            userService.updateById(userDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 获取个人信息
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/getPersonInfo", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getPersonInfo(){
        try{
            User currentUser = UserContext.getCurrentUser();
            Integer id = currentUser.getId();
            UserDo byId = userService.getById(id);
            return AjaxResult.success(byId);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }


    /**
     * 管理员查看用户邀请人列表总数
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/adminInvitationUserCount", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult adminInvitationUserCount(@RequestParam Integer userId){
        try{
            UserDo byId = userService.getById(userId);
            QueryWrapper<UserDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            long count = userService.count(wq);
            return AjaxResult.success(count);

        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 管理员查看用户的邀请人列表
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/adminInvitationUserList", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult adminInvitationUserList(@RequestParam Integer userId){
        try{
            UserDo byId = userService.getById(userId);
            QueryWrapper<UserDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            List<UserDo> list = userService.list(wq);
            return AjaxResult.success(list);

        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
    /**
     * 管理员查看用户总收益
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/adminInvitationUserTotalPrice", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult adminInvitationUserTotalPrice(@RequestParam Integer userId){
        try{
            UserDo byId = userService.getById(userId);
            QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            wq.eq("status", Constant.InvitationStatus.SUCESS);
            List<TripOrderDo> list = tripOrderServiceImpl.list(wq);
            if(list!=null&&list.size()>0){
                //计算今日预估积分费用
                StringBuilder sb=new StringBuilder();
                for (TripOrderDo tripOrderDo : list) {
                    String invitationFee = tripOrderDo.getInvitationFee();
                    sb.append(invitationFee).append("+");
                }
                if(sb.toString().endsWith("+")){
                    BigDecimal cal = CalculateUtil.cal(sb.append("0").toString());
                    return AjaxResult.success(cal);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

        return AjaxResult.success(0);

    }
    /**
     * 管理员查看用户么个邀请人的收益
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/adminInvitationUserOnePersonTotalPrice", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult adminInvitationUserOnePersonTotalPrice(@RequestParam Integer userId,@RequestParam Integer invitationUserId){
        try{
            UserDo byId = userService.getById(userId);
            QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            wq.eq("user_id",invitationUserId);
            wq.eq("status", Constant.InvitationStatus.SUCESS);
            List<TripOrderDo> list = tripOrderServiceImpl.list(wq);
            if(list!=null&&list.size()>0){
                //计算今日预估积分费用
                StringBuilder sb=new StringBuilder();
                for (TripOrderDo tripOrderDo : list) {
                    String invitationFee = tripOrderDo.getInvitationFee();
                    sb.append(invitationFee).append("+");
                }
                if(sb.toString().endsWith("+")){
                    BigDecimal cal = CalculateUtil.cal(sb.append("0").toString());
                    return AjaxResult.success(cal);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

        return AjaxResult.success(0);

    }
    /**
     * 管理员查看用户么个邀请人的收益订单详情
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/adminInvitationUserOnePersonList", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult adminInvitationUserOnePersonList(@RequestParam Integer userId,@RequestParam Integer invitationUserId){
        try{
            UserDo byId = userService.getById(userId);
            QueryWrapper<TripOrderDo> wq=new QueryWrapper<>();
            wq.eq("invitation_openid",byId.getOpenid());
            wq.eq("user_id",invitationUserId);
            wq.eq("status", Constant.InvitationStatus.SUCESS);
            List<TripOrderDo> list = tripOrderServiceImpl.list(wq);
            return AjaxResult.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }


}

