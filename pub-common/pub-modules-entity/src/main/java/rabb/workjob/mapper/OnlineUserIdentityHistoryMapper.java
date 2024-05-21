package rabb.workjob.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rabb.workjob.entity.OnlineUserIdentityHistoryDo;

/**
 * <p>
 * 用户认证历史记录表 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-21
 */
@Mapper
public interface OnlineUserIdentityHistoryMapper extends BaseMapper<OnlineUserIdentityHistoryDo> {

}
