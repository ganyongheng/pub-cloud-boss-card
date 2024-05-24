package work.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.config.TimingLog;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rabb.workjob.dto.WorkConstantDto;
import rabb.workjob.entity.OnlineImageNewsDo;
import work.service.impl.OnlineImageNewsServiceImpl;

import java.util.List;

/**
 * <p>
 * 轮播图 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-24
 */
@Controller
@RequestMapping("/onlinework/onlineImageNewsDo")
public class OnlineImageNewsController extends BaseController {

    @Autowired
    private OnlineImageNewsServiceImpl onlineImageNewsServiceImpl;

    /**
     * 获取轮播图接口
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getList(){
        try{
            QueryWrapper<OnlineImageNewsDo> wq=new QueryWrapper<>();
            wq.eq("status", WorkConstantDto.imageAble.yes);
            List<OnlineImageNewsDo> list = onlineImageNewsServiceImpl.list(wq);
            return AjaxResult.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }
}

