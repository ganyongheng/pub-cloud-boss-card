package work.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.util.page.TableDataInfo;
import com.pub.redis.util.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import rabb.workjob.entity.OnlineWorkDo;
import work.service.impl.OnlineUserServiceImpl;
import work.service.impl.OnlineWorkServiceImpl;
import work.service.impl.OnlineWorkUserServiceImpl;

import java.util.List;

/**
 * <p>
 * 工作发布信息 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Controller
@RequestMapping("/onlinework/onlineWorkDo")
public class OnlineWorkController extends BaseController {

    @Autowired
    private OnlineWorkServiceImpl onlineWorkService;

    @Autowired
    private RedisCache redisCache;


    /**
     * 发布工作
     */
    @TimingLog
    @RequestMapping(value = "/releaseWork", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult releaseWork(@RequestBody OnlineWorkDo onlineWorkDo){
        try{
            /**
             * 校验短信验证码
             */
            String phone = onlineWorkDo.getPhone();
            String phoneCode = onlineWorkDo.getPhoneCode();
            String stringCache = redisCache.getStringCache(phone);
            if(StringUtils.isNotBlank(stringCache)&&stringCache.equals(phoneCode)){
                onlineWorkService.releaseWork(onlineWorkDo);
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
     * 获取我的发布信息
     */
    @TimingLog
    @RequestMapping(value = "/getMyReleaseWorkList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getMyReleaseWorkList(@RequestBody JSONObject req){
        try{
            List<OnlineWorkDo> pageList =  onlineWorkService.getMyReleaseWorkList(req);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }


    /**
     * 修改发布工作
     */
    @TimingLog
    @RequestMapping(value = "/editReleaseWork", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult editReleaseWork(@RequestBody OnlineWorkDo onlineWorkDo){
        try{
            /**
             * 校验短信验证码
             */
            String phone = onlineWorkDo.getPhone();
            String phoneCode = onlineWorkDo.getPhoneCode();
            String stringCache = redisCache.getStringCache(phone);
            if(StringUtils.isNotBlank(stringCache)&&stringCache.equals(phoneCode)){
                onlineWorkService.editReleaseWork(onlineWorkDo);
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
     * 获取职位发布列表
     */
    @TimingLog
    @RequestMapping(value = "/getPublicReleaseWorkList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getPublicReleaseWorkList(@RequestBody OnlineWorkDo onlineWorkDo){
        try{
            List<OnlineWorkDo> pageList =  onlineWorkService.getPublicReleaseWorkList(onlineWorkDo);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 获取职位详情
     */
    @TimingLog
    @RequestMapping(value = "/getWorkDetail", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getWorkDetail(@RequestParam Integer id){
        try{
            OnlineWorkDo byId = onlineWorkService.getById(id);
            return AjaxResult.success(byId);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

}

