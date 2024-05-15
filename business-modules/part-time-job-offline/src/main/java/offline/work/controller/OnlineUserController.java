package offline.work.controller;


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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rabb.workjob.entity.OnlineUserDo;


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

}

