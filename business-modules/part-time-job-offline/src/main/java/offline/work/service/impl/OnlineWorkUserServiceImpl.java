package offline.work.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.auth.entity.User;
import com.cn.auth.util.UserContext;
import com.pub.core.exception.BusinessException;
import com.pub.core.util.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rabb.workjob.entity.OnlineResumeDo;
import rabb.workjob.entity.OnlineUserDo;
import rabb.workjob.entity.OnlineWorkDo;
import rabb.workjob.entity.OnlineWorkUserDo;
import rabb.workjob.mapper.OnlineWorkUserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 用户投递信息 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Service
public class OnlineWorkUserServiceImpl extends ServiceImpl<OnlineWorkUserMapper, OnlineWorkUserDo> implements IService<OnlineWorkUserDo> {


}
