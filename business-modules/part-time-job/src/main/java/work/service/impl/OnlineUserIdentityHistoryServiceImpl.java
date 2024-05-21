package work.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rabb.workjob.entity.OnlineUserIdentityHistoryDo;
import rabb.workjob.mapper.OnlineUserIdentityHistoryMapper;
import rabb.workjob.service.IOnlineUserIdentityHistoryService;

/**
 * <p>
 * 用户认证历史记录表 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-21
 */
@Service
public class OnlineUserIdentityHistoryServiceImpl extends ServiceImpl<OnlineUserIdentityHistoryMapper, OnlineUserIdentityHistoryDo> implements IOnlineUserIdentityHistoryService {

}
