package work.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rabb.workjob.entity.SysDataDictionaryDo;
import work.service.impl.SysDataDictionaryServiceImpl;

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
@RequestMapping("/onlinework/sysDataDictionaryDo")
public class SysDataDictionaryController {
    @Autowired
    private SysDataDictionaryServiceImpl sysDataDictionaryServiceImpl;

    private JSONArray jsonObject_third;

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
    @RequestMapping(value = "/getAddressThird11", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getAddressThirdTest() {
        try{
            String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("address_third_test", "address_third_test");
            JSONArray jsonObject = JSONArray.parseArray(sysBaseParam);
            for (Object o : jsonObject) {
                JSONObject js= (JSONObject) o;
                String name = js.getString("name");
                if(StringUtils.isNotBlank(name)){
                    js.put("text",name);
                    js.put("value",name);
                    JSONArray city = js.getJSONArray("city");
                    if(city!=null&& !city.isEmpty()){
                        js.put("children",city);
                        js.remove("city");
                        js.remove("name");
                        for (Object object : city) {
                            JSONObject js_object= (JSONObject) object;
                            String name_js = js_object.getString("name");
                            js_object.put("text",name_js);
                            js_object.put("value",name_js);
                            JSONArray area = js_object.getJSONArray("area");
                            if(!area.isEmpty()){
                                JSONArray jsonArray_tem=new JSONArray();
                                for (Object o1 : area) {
                                    JSONObject js_temp=new JSONObject();
                                    js_temp.put("text",o1);
                                    js_temp.put("value",o1);
                                    jsonArray_tem.add(js_temp);
                                }
                                js_object.put("children",jsonArray_tem);
                                js_object.remove("area");
                            }
                            js_object.remove("name");
                        }
                    }

                }
            }
            System.out.println(jsonObject.toJSONString());
            return AjaxResult.success(jsonObject.toJSONString());
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error();
        }
    }
    /**
     * 获取退费规则的配置
     * @return
     */
    @RequestMapping(value = "/getAddressThird", method = RequestMethod.GET)
    @ResponseBody
    public AjaxResult getAddressThird() {
        try{
            if(jsonObject_third==null){
                String sysBaseParam = sysDataDictionaryServiceImpl.getSysBaseParam("address_third", "address_third");
                jsonObject_third = JSONArray.parseArray(sysBaseParam);
            }

            return AjaxResult.success(jsonObject_third);
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

