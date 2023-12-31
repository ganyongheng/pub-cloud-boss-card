package com.cn.school.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.school.config.FilePathOnlineConfig;
import com.cn.school.entity.TripAreaDo;
import com.cn.school.entity.TripCarDo;
import com.cn.school.entity.TripProductCarRelationDo;
import com.cn.school.entity.TripProductDo;
import com.cn.school.mapper.TripAreaMapper;
import com.cn.school.service.ITripAreaService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.DateUtils;
import com.pub.core.utils.StringUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

/**
 * <p>
 * 行程表 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-16
 */
@Service
public class TripAreaServiceImpl extends ServiceImpl<TripAreaMapper, TripAreaDo> implements ITripAreaService {

    @Autowired
    private TripProductServiceImpl tripProductService;
    @Autowired
    private TripCarServiceImpl tripCarServiceImpl;
    @Autowired
    private TripProductCarRelationServiceImpl tripProductCarRelationServiceImpl;

    public void addTripAreaDo(TripAreaDo tripAreaDo) throws Exception{
        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        wq.eq("origin",tripAreaDo.getOrigin());
        wq.eq("destination",tripAreaDo.getDestination());
        TripAreaDo one = getOne(wq);
        if(one!=null){
            throw new BusinessException("线路已存在！");
        }
        tripAreaDo.setCreateTime(new Date());
        tripAreaDo.setDeleteStatus(9);
        save(tripAreaDo);
    }

    public Map<String,Set<String>> startTrips() {
        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        wq.eq("delete_status",9);
        List<TripAreaDo> list = list(wq);
        Map<String,Set<String>> map=new HashedMap();
        for (TripAreaDo tripAreaDo : list) {
            String cityName = tripAreaDo.getCityName();
            Set<String> tripAreaDos = map.get(cityName);
            if(tripAreaDos==null){
                tripAreaDos=new HashSet<>();
                map.put(cityName,tripAreaDos);
            }
            String origin = tripAreaDo.getOrigin();
            tripAreaDos.add(origin);
        }
        return map;
    }

    public Map<String, List<TripAreaDo>> endTrips(String startTrips) {
        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        wq.eq("origin",startTrips);
        wq.eq("delete_status",9);
        List<TripAreaDo> list = list(wq);
        Map<String,List<TripAreaDo>> map=new HashedMap();
        for (TripAreaDo tripAreaDo : list) {
            String destinationCityName = tripAreaDo.getDestinationCityName();
            List<TripAreaDo> tripAreaDos = map.get(destinationCityName);
            if(tripAreaDos==null){
                tripAreaDos=new ArrayList<>();
                map.put(destinationCityName,tripAreaDos);
            }
            tripAreaDos.add(tripAreaDo);
        }
        return map;
    }

    public List<TripAreaDo> gethotTrips() {
        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        wq.eq("is_hot",1);
        wq.eq("delete_status",9);
        List<TripAreaDo> list = list(wq);
        return list;
    }

    public List<TripAreaDo> getPageList(TripAreaDo req) {

        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        String origin = req.getOrigin();
        if(StringUtils.isNotBlank(origin)){
            wq.like("origin",origin);
        }
        String destination = req.getDestination();
        if(StringUtils.isNotBlank(destination)){
            wq.like("destination",destination);
        }
        String cityName = req.getCityName();
        if(StringUtils.isNotBlank(cityName)){
            wq.like("city_name",cityName);
        }
        Integer isHot = req.getIsHot();
        if(isHot!=null){
            wq.eq("is_hot",isHot);
        }
        Integer deleteStatus = req.getDeleteStatus();
        if(deleteStatus!=null){
            wq.eq("delete_status",deleteStatus);
        }
        wq.orderByDesc("city_name","create_time");
        BaseController.startPage();
        List<TripAreaDo> list = list(wq);
        return list;
    }

    public List<Map> findTrips(Integer trip_area_id,String data) {
        return tripProductCarRelationServiceImpl.findTrips(trip_area_id, data);
    }

    public void editTripAreaDo(TripAreaDo tripAreaDo) throws Exception{
        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        wq.eq("origin",tripAreaDo.getOrigin());
        wq.eq("destination",tripAreaDo.getDestination());
        wq.ne("id",tripAreaDo.getId());
        TripAreaDo one = getOne(wq);
        if(one!=null){
            throw new BusinessException("线路已存在！");
        }
        updateById(tripAreaDo);
    }
    public void deleteTripAreaDo(TripAreaDo tripAreaDo) throws Exception{
        updateById(tripAreaDo);
    }

    public List<TripAreaDo> getCheckBox() {
        QueryWrapper<TripAreaDo> wq=new QueryWrapper<>();
        wq.eq("delete_status",9);
        List<TripAreaDo> list = list(wq);
        /**
         * 获取车次价格表
         */
        for (TripAreaDo tripAreaDo : list) {
            Integer id = tripAreaDo.getId();
            QueryWrapper<TripProductDo> wqTripProductDo=new QueryWrapper<>();
            wqTripProductDo.eq("trip_area_id",id);
            wqTripProductDo.eq("delete_status",9);
            List<TripProductDo> listTripProductDo = tripProductService.list(wqTripProductDo);
            tripAreaDo.setListTripProductDo(listTripProductDo);
        }
        return list;
    }

    @Autowired
    private FilePathOnlineConfig filePathOnlineConfig;

    public String uploadImage(MultipartFile file) throws Exception {
        //存储文件路径
        //存储文件路径
        String root = filePathOnlineConfig.getRoot();
        String baseUrl = filePathOnlineConfig.getBaseUrl();
        String originalFilename = DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS,new Date())+file.getOriginalFilename();
        String absolutePath =root+"/"+originalFilename;
        File extracted = extracted(file, root);
        String[] split = absolutePath.split(root);
        if(split.length>1){
            return baseUrl+split[1];
        }
        return null;
    }
    public  File extracted(MultipartFile multipartFile, String path) throws Exception {
        String originalFilename = DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS,new Date())+multipartFile.getOriginalFilename();
        File localFile = new File(path +"/"+ originalFilename);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        multipartFile.transferTo(localFile);
        if (multipartFile.isEmpty()) {
            multipartFile.getInputStream().close();
        }
        return localFile;
    }

}
