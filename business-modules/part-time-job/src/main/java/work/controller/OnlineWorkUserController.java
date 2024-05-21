package work.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.util.page.TableDataInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rabb.workjob.entity.OnlineWorkDo;
import rabb.workjob.entity.OnlineWorkUserDo;
import work.service.impl.OnlineWorkUserServiceImpl;

import java.util.List;

/**
 * <p>
 * 用户投递信息 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Controller
@RequestMapping("/onlinework/onlineWorkUserDo")
public class OnlineWorkUserController extends BaseController {

    @Autowired
    private OnlineWorkUserServiceImpl onlineWorkUserService;

    /**
     * 投递简历工作
     */
    @TimingLog
    @RequestMapping(value = "/submitReleaseWork", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult submitReleaseWork(@RequestBody OnlineWorkUserDo onlineWorkUserDo){
        try{
            onlineWorkUserService.submitReleaseWork(onlineWorkUserDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取我投递的工作
     */
    @TimingLog
    @RequestMapping(value = "/getMySubmitWorkList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getMySubmitWorkList(@RequestBody JSONObject req){
        try{
            List<OnlineWorkDo> pageList =  onlineWorkUserService.getMySubmitWorkList(req);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 获取投递我的
     */
    @TimingLog
    @RequestMapping(value = "/getWorkSubmitUserList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getWorkSubmitUserList(@RequestBody JSONObject req){
        try{
            List<OnlineWorkDo> pageList =  onlineWorkUserService.getWorkSubmitUserList(req);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
}

