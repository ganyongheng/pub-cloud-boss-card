package offline.work.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import offline.work.service.impl.OnlineImageNewsServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import rabb.workjob.entity.OfflineUserDo;
import rabb.workjob.entity.OnlineImageNewsDo;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 轮播图 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-24
 */
@Controller
@RequestMapping("/offlinework/onlineImageNewsDo")
public class OnlineImageNewsController extends BaseController {

    @Autowired
    private OnlineImageNewsServiceImpl onlineImageNewsService;

    /**
     * 添加轮播图数据
     * @return
     */
    @RequestMapping(value = "/addImage", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult addImage(@RequestBody JSONArray jsonArray){
        try{
            onlineImageNewsService.addImage(jsonArray);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 上线下线接口
     * @return
     */
    @RequestMapping(value = "/upOrDowm", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult upOrDowm(@RequestBody OnlineImageNewsDo onlineImageNewsDo){
        try{
            onlineImageNewsService.upOrDowm(onlineImageNewsDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 删除接口
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult delete(@RequestParam Integer id){
        try{
            onlineImageNewsService.removeById(id);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

}

