package rabb.workjob.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rabb.workjob.entity.OnlineUserDo;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Mapper
public interface OnlineUserMapper extends BaseMapper<OnlineUserDo> {

}
