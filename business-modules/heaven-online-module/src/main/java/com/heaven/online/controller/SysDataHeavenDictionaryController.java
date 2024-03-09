package com.heaven.online.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heaven.online.service.OnlineHeavenShopDetailServiceImpl;
import com.heaven.online.service.SysDataHeavenDictionaryServiceImpl;
import com.pub.core.util.controller.BaseController;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rabb.shop.entity.SysDataDictionaryDo;

import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 系统数据字典数据库 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-01
 */
@Controller
@RequestMapping("/sysDataHeavenDictionaryDo")
public class SysDataHeavenDictionaryController extends BaseController {
    @Autowired
    private SysDataHeavenDictionaryServiceImpl sysDataDictionaryServiceImpl;

    @RequestMapping(value = "/refreshCache", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult refreshCache() {
        try{
            sysDataDictionaryServiceImpl.refreshCache();
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    /**
     * 下拉选框
     * @param key
     * @return
     */
    @RequestMapping(value = "/getCheckBox", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult getRoleTypeCheckBox(@RequestParam String key) {
        try{
            Map<String, String> sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam(key);
            return AjaxResult.success(sysBaseParam);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }



}

