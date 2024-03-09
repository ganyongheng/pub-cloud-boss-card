package com.heaven.online.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import rabb.heaven.entity.OnlineHeavenShopDetailDo;
import rabb.heaven.entity.OnlineHeavenShopDo;
import rabb.heaven.mapper.OnlineHeavenShopMapper;
import rabb.heaven.service.IOnlineHeavenShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 纪念馆 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-03-09
 */
@Service
public class OnlineHeavenShopServiceImpl extends ServiceImpl<OnlineHeavenShopMapper, OnlineHeavenShopDo> implements IOnlineHeavenShopService {
    @Autowired
    private OnlineHeavenShopDetailServiceImpl onlineHeavenShopDetailService;

    public void addHeavenShopDo(OnlineHeavenShopDo onlineHeavenShopDo) {
        User currentUser = UserContext.getCurrentUser();
        Date date = new Date();
        onlineHeavenShopDo.setCreateTime(date);
        onlineHeavenShopDo.setUpdateTime(date);
        //初始化
        onlineHeavenShopDo.setIsCheck(0);
        Integer type = onlineHeavenShopDo.getType();
        Integer isMarty = onlineHeavenShopDo.getIsMarty();
        if(1==type&&1==isMarty){
            //如果是烈士，默认是公开的
            onlineHeavenShopDo.setIsPublic(1);
        }else{
            onlineHeavenShopDo.setIsPublic(0);
        }
        onlineHeavenShopDo.setUserId(1);
        save(onlineHeavenShopDo);
        List<OnlineHeavenShopDetailDo> listDetail = onlineHeavenShopDo.getListDetail();
        if(listDetail!=null&&listDetail.size()>0){
            for (OnlineHeavenShopDetailDo onlineHeavenShopDetailDo : listDetail) {
                onlineHeavenShopDetailDo.setCreateTime(date);
                onlineHeavenShopDetailDo.setUpdateTime(date);
                onlineHeavenShopDetailDo.setParentId(onlineHeavenShopDo.getId());
            }
            onlineHeavenShopDetailService.saveBatch(listDetail);
        }

    }

    public List<OnlineHeavenShopDo> listHeavenShopDo(OnlineHeavenShopDo onlineHeavenShopDo) {
        User currentUser = UserContext.getCurrentUser();
        QueryWrapper<OnlineHeavenShopDo> wq=new QueryWrapper<>();
        wq.eq("user_id",1);
        List<OnlineHeavenShopDo> list = list(wq);
        for (OnlineHeavenShopDo heavenShopDo : list) {
            Integer id = heavenShopDo.getId();
            QueryWrapper<OnlineHeavenShopDetailDo> wq_detail=new QueryWrapper<>();
            wq_detail.eq("parent_id",id);
            List<OnlineHeavenShopDetailDo> listOnlineHeavenShopDetailDo = onlineHeavenShopDetailService.list(wq_detail);
            heavenShopDo.setListDetail(listOnlineHeavenShopDetailDo);
        }
        return list;
    }

    public List<OnlineHeavenShopDo> listPubHeavenShopDo(OnlineHeavenShopDo onlineHeavenShopDo) {
        QueryWrapper<OnlineHeavenShopDo> wq=new QueryWrapper<>();
        wq.eq("is_public",1);
        List<OnlineHeavenShopDo> list = list(wq);
        for (OnlineHeavenShopDo heavenShopDo : list) {
            Integer id = heavenShopDo.getId();
            QueryWrapper<OnlineHeavenShopDetailDo> wq_detail=new QueryWrapper<>();
            wq_detail.eq("parent_id",id);
            List<OnlineHeavenShopDetailDo> listOnlineHeavenShopDetailDo = onlineHeavenShopDetailService.list(wq_detail);
            heavenShopDo.setListDetail(listOnlineHeavenShopDetailDo);
        }
        return list;
    }
}
