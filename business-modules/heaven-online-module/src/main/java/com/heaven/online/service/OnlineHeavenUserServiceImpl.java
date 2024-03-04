package com.heaven.online.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.auth.config.jwt.TokenProvider;
import com.cn.auth.entity.User;
import com.pub.core.common.OnlineConstants;
import com.pub.core.util.controller.BaseController;
import com.pub.core.utils.CalculateUtil;
import com.pub.core.utils.StringUtils;
import com.pub.redis.util.RedisCache;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rabb.heaven.entity.OnlineHeavenUserDo;
import rabb.heaven.mapper.OnlineHeavenUserMapper;
import rabb.heaven.service.IOnlineHeavenUserService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-14
 */
@Log4j2
@Service
public class OnlineHeavenUserServiceImpl extends ServiceImpl<OnlineHeavenUserMapper, OnlineHeavenUserDo> implements IOnlineHeavenUserService {


}
