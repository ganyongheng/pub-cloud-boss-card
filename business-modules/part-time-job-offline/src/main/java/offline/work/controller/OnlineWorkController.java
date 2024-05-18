package offline.work.controller;
import com.cn.auth.config.TimingLog;
import com.pub.core.util.controller.BaseController;

import com.pub.core.util.domain.AjaxResult;
import com.pub.core.util.page.TableDataInfo;
import offline.work.service.impl.OnlineWorkServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rabb.workjob.entity.OnlineWorkDo;

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
@RequestMapping("/offlinework/onlineWorkDo")
public class OnlineWorkController extends BaseController {

    private OnlineWorkServiceImpl onlineWorkService;

    /**
     * 获取职位列表
     */
    @TimingLog
    @RequestMapping(value = "/getOfflineWorkList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getOfflineWorkList(@RequestBody OnlineWorkDo onlineWorkDo){
        try{
            List<OnlineWorkDo> pageList =  onlineWorkService.getOfflineWorkList(onlineWorkDo);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 认证工作
     */
    @TimingLog
    @RequestMapping(value = "/identityWorkList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult identityWorkList(@RequestBody OnlineWorkDo onlineWorkDo){
        try{
             onlineWorkService.identityWorkList(onlineWorkDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }


}

