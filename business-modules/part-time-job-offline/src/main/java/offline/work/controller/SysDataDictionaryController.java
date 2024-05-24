package offline.work.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.config.TimingLog;
import com.pub.core.util.domain.AjaxResult;
import com.pub.core.utils.DateUtils;
import com.pub.core.utils.StringUtils;
import offline.work.config.FilePathOfflineConfig;
import offline.work.service.impl.SysDataDictionaryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import rabb.workjob.entity.SysDataDictionaryDo;

import java.io.File;
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

    @Autowired
    private FilePathOfflineConfig filePathOfflineConfig;

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
     * 上传文件
     * @return
     */
    @TimingLog
    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResult uploadImage(MultipartFile file){
        try{
            String url= uploadImageFile(file);
            return AjaxResult.success(url);
        }catch (Exception e){
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }

    }

    public String uploadImageFile(MultipartFile file) throws Exception {
        //存储文件路径
        String root = filePathOfflineConfig.getRoot();
        String baseUrl = filePathOfflineConfig.getBaseUrl();
        String originalFilename = DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS,new Date())+file.getOriginalFilename();
        File localFile = new File(root +"/"+ originalFilename);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        file.transferTo(localFile);
        if (file.isEmpty()) {
            file.getInputStream().close();
        }
        return baseUrl+"/"+originalFilename;
    }



}

