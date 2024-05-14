package work.service.impl;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.auth.config.Constant;
import com.pub.core.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import rabb.workjob.entity.SysDataDictionaryDo;
import rabb.workjob.mapper.SysDataDictionaryMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统数据字典数据库 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-01
 */
@Service
public class SysDataDictionaryServiceImpl extends ServiceImpl<SysDataDictionaryMapper, SysDataDictionaryDo> implements IService<SysDataDictionaryDo> {


    private static Log log = LogFactory.getLog(SysDataDictionaryServiceImpl.class);

    private Map<String, Map<String, String>>  cacheMap;

    /**
     * 根据参数名获取参数信息
     * @param paramName
     */
    public String getSysBaseParam(String paramName, String key) throws Exception{
        if(StringUtils.isBlank(paramName)){
            throw new BusinessException("paramName名称不能为空");
        }
        if(StringUtils.isBlank(key)){
            throw new BusinessException("key名称不能为空");
        }

        Map<String, String> map = this.getSysBaseParam(paramName);
        if(map==null || map.isEmpty() || !map.containsKey(key)){
            return null;
        }
        String paranValue = map.get(key);

        //查询参数
        log.info("cache paramName:"+paramName+", key:"+key+", paranValue: "+ paranValue);
        //返回
        return paranValue;
    }


    /**
     * 查询系统参数返回的map
     * @param paramName
     * @return
     * @throws Exception
     */
    public Map<String, String> getSysBaseParam(String paramName) throws Exception {
        if(cacheMap==null){
            getCache();
        }
        if(!cacheMap.containsKey(paramName)){
            return null;
        }

        Map<String, String> paramMap = cacheMap.get(paramName);
        return paramMap;
    }


    public Map<String, Map<String, String>> getCache(){
        if(cacheMap==null){
            synchronized ("cacheMap"){
                if(cacheMap==null){
                    cacheMap=new HashMap<>();
                    QueryWrapper<SysDataDictionaryDo> qw=new QueryWrapper<>();
                    qw.eq("status",Constant.status.online);
                    qw.orderByAsc("id");
                    List<SysDataDictionaryDo> list = list(qw);
                    for (SysDataDictionaryDo sysDataDictionaryDo : list) {
                        String paramName = sysDataDictionaryDo.getParamName();
                        String paramValue = sysDataDictionaryDo.getParamValue();
                        String paramKey = sysDataDictionaryDo.getParamKey();
                        Map<String, String> stringStringMap = cacheMap.get(paramName);
                        if(stringStringMap==null){
                            stringStringMap=new HashMap<>();
                            stringStringMap.put(paramKey,paramValue);
                            cacheMap.put(paramName,stringStringMap);
                        }else{
                            stringStringMap.put(paramKey,paramValue);
                        }
                    }
                }
            }
        }

        return cacheMap;
    }

    public void refreshCache(){
        cacheMap=new HashMap<>();
        QueryWrapper<SysDataDictionaryDo> qw=new QueryWrapper<>();
        qw.eq("status", Constant.status.online);
        qw.orderByAsc("id");
        List<SysDataDictionaryDo> list = list(qw);
        for (SysDataDictionaryDo sysDataDictionaryDo : list) {
            String paramName = sysDataDictionaryDo.getParamName();
            String paramValue = sysDataDictionaryDo.getParamValue();
            String paramKey = sysDataDictionaryDo.getParamKey();
            Map<String, String> stringStringMap = cacheMap.get(paramName);
            if(stringStringMap==null){
                stringStringMap=new HashMap<>();
                stringStringMap.put(paramKey,paramValue);
                cacheMap.put(paramName,stringStringMap);
            }else{
                stringStringMap.put(paramKey,paramValue);
            }
        }
    }
}
