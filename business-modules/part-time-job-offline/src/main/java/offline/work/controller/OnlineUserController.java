package offline.work.controller;


import com.alibaba.fastjson.JSONObject;
import com.cn.auth.config.TimingLog;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.util.page.TableDataInfo;
import com.pub.core.utils.RandomUtilSendMsg;
import com.pub.redis.util.RedisCache;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import offline.work.service.impl.OnlineUserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rabb.workjob.entity.OnlineUserDo;
import rabb.workjob.entity.OnlineWorkDo;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Controller
@RequestMapping("/offlinework/onlineUserDo")
public class OnlineUserController extends BaseController {

    @Autowired
    private OnlineUserServiceImpl onlineUserService;

    /**
     * 获取用户列表
     */
    @TimingLog
    @RequestMapping(value = "/getOfflineOnlineUserList", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getOfflineWorkList(@RequestBody OnlineUserDo onlineUserDo){
        try{
            List<OnlineUserDo> pageList =  onlineUserService.getOfflineOnlineUserList(onlineUserDo);
            TableDataInfo dataTable = getDataTable(pageList);
            return AjaxResult.success(dataTable);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 认证用户审核
     */
    @TimingLog
    @RequestMapping(value = "/identityOnlineUser", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult identityWorkList(@RequestBody OnlineUserDo onlineUserDo){
        try{
            onlineUserService.identityOnlineUser(onlineUserDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

}

