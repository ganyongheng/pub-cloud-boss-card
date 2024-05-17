package rabb.workjob.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import rabb.workjob.entity.OfflineUserDo;

/**
 * <p>
 * 离线用户 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-16
 */
@Mapper
public interface OfflineUserMapper extends BaseMapper<OfflineUserDo> {

}
