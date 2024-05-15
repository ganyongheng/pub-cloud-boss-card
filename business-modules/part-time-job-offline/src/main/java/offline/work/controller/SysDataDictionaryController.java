package offline.work.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.StringUtils;
import offline.work.service.impl.SysDataDictionaryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import rabb.workjob.entity.SysDataDictionaryDo;

import java.util.Date;

/**
 * <p>
 * 系统数据字典数据库 前端控制器
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-01
 */
@Controller
@RequestMapping("/offlinework/sysDataDictionaryDo")
public class SysDataDictionaryController {
    @Autowired
    private SysDataDictionaryServiceImpl sysDataDictionaryServiceImpl;

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
     * 获取退费规则的配置
     * @return
     */
    @RequestMapping(value = "/getRefundsMsg", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getRefundsMsg() {
        try{
            String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("refunds_notice_msg", "refunds_notice_msg");
            return AjaxResult.success(sysBaseParam,"成功");
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    /**
     * 添加退费规则的配置
     * @return
     */
    @RequestMapping(value = "/addTicketsNum", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult addTicketsNum(@RequestParam Integer num) {
        try{
            QueryWrapper<SysDataDictionaryDo> wq=new QueryWrapper<>();
            wq.eq("param_key","tickets_num");
            wq.eq("param_name","tickets_num");
            SysDataDictionaryDo one = sysDataDictionaryServiceImpl.getOne(wq);
            if(one==null){
                one=new SysDataDictionaryDo();
                one.setParamKey("tickets_num");
                one.setParamName("tickets_num");
                one.setParamValue(num+"");
                one.setParamDesc("剩余车票最小值告警");
                one.setStatus(9);
                one.setCreateDate(new Date());
                sysDataDictionaryServiceImpl.save(one);
            }else{
                one.setCreateDate(new Date());
                one.setParamValue(num+"");
                sysDataDictionaryServiceImpl.updateById(one);
            }
            sysDataDictionaryServiceImpl.refreshCache();
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }


    /**
     * 获取退费规则的配置
     * @return
     */
    @RequestMapping(value = "/getTicketsNum", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getTicketsNum() {
        try{
            String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("tickets_num", "tickets_num");
            return AjaxResult.success("成功",sysBaseParam);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }
    /**
     * 获取学校下拉框
     * @return
     */
    @RequestMapping(value = "/getSchoolName", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getSchool() {
        try{
            String school_name = sysDataDictionaryServiceImpl.getSysBaseParam("school_name", "school_name");
            String[] split = school_name.split("##");
            return AjaxResult.success("成功",split);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }
    /**
     * 添加学校下拉框
     * @return
     */
    @RequestMapping(value = "/addSchoolName", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult addSchoolName(@RequestParam String schoolName) {
        try{
            QueryWrapper<SysDataDictionaryDo> wq=new QueryWrapper<>();
            wq.eq("param_key","school_name");
            wq.eq("param_name","school_name");
            SysDataDictionaryDo one = sysDataDictionaryServiceImpl.getOne(wq);
            String school_name = one.getParamValue();
           StringBuilder sb=new StringBuilder();
           if(StringUtils.isNotBlank(school_name)){
               sb.append(school_name).append("##").append(schoolName);
           }
            one.setParamValue(sb.toString());
            sysDataDictionaryServiceImpl.updateById(one);
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    /**
     * 提现积分配置
     * @return
     */
    @RequestMapping(value = "/addIntegralManageNum", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult addIntegralManageNum(@RequestParam String num) {
        try{
            QueryWrapper<SysDataDictionaryDo> wq=new QueryWrapper<>();
            wq.eq("param_key","integral_manage_num");
            wq.eq("param_name","integral_manage_num");
            SysDataDictionaryDo one = sysDataDictionaryServiceImpl.getOne(wq);
            if(one==null){
                one=new SysDataDictionaryDo();
                one.setParamKey("integral_manage_num");
                one.setParamName("integral_manage_num");
                one.setParamValue(num);
                one.setParamDesc("最小提现积分配置！");
                one.setStatus(9);
                one.setCreateDate(new Date());
                sysDataDictionaryServiceImpl.save(one);
            }else{
                one.setCreateDate(new Date());
                one.setParamValue(num);
                sysDataDictionaryServiceImpl.updateById(one);
            }
            sysDataDictionaryServiceImpl.refreshCache();
            return AjaxResult.success();
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }

    /**
     * 获取学校下拉框
     * @return
     */
    @RequestMapping(value = "/getIntegralManageNum", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getIntegralManageNum() {
        try{
            String integral_manage_num = sysDataDictionaryServiceImpl.getSysBaseParam("integral_manage_num", "integral_manage_num");
            return AjaxResult.success("成功",integral_manage_num);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }
}

