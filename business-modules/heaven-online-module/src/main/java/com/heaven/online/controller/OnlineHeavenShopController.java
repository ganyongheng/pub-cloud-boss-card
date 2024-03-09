package com.heaven.online.controller;


import com.cn.auth.config.TimingLog;
import com.heaven.online.service.OnlineHeavenShopServiceImpl;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rabb.heaven.entity.OnlineHeavenShopDo;

import java.util.List;

/**
 * <p>
 * 纪念馆 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2024-03-09
 */
@Controller
@RequestMapping("/onlineHeavenShopDo")
public class OnlineHeavenShopController extends BaseController {
    @Autowired
    private OnlineHeavenShopServiceImpl onlineHeavenShopService;

    /**
     * 新增墓地
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/addHeavenShopDo", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult addHeavenShopDo(@RequestBody OnlineHeavenShopDo onlineHeavenShopDo){
        try{
            onlineHeavenShopService.addHeavenShopDo(onlineHeavenShopDo);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 我的纪念馆
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/listHeavenShopDo", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult listHeavenShopDo(@RequestBody OnlineHeavenShopDo onlineHeavenShopDo){
        try{
            List<OnlineHeavenShopDo> onlineHeavenShopDos = onlineHeavenShopService.listHeavenShopDo(onlineHeavenShopDo);
            return AjaxResult.success(onlineHeavenShopDos);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
    /**
     * 公开纪念馆
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/listPubHeavenShopDo", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult listPubHeavenShopDo(@RequestBody OnlineHeavenShopDo onlineHeavenShopDo){
        try{
            List<OnlineHeavenShopDo> onlineHeavenShopDos = onlineHeavenShopService.listPubHeavenShopDo(onlineHeavenShopDo);
            return AjaxResult.success(onlineHeavenShopDos);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
}

