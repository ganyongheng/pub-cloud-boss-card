package work.service.impl;

import work.entity.OnlineWorkDo;
import work.mapper.OnlineWorkMapper;
import work.service.IOnlineWorkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工作发布信息 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Service
public class OnlineWorkServiceImpl extends ServiceImpl<OnlineWorkMapper, OnlineWorkDo> implements IOnlineWorkService {

}
