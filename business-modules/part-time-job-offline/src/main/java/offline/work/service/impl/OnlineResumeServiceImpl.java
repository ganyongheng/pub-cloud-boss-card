package offline.work.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import rabb.workjob.entity.OnlineResumeDo;
import rabb.workjob.mapper.OnlineResumeMapper;

/**
 * <p>
 * 个人简历 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Service
public class OnlineResumeServiceImpl extends ServiceImpl<OnlineResumeMapper, OnlineResumeDo> implements IService<OnlineResumeDo> {

}
