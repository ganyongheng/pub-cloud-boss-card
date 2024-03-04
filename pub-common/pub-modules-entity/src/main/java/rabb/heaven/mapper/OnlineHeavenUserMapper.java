package rabb.heaven.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import rabb.heaven.entity.OnlineHeavenUserDo;

/**
 * <p>
 * 在线用户表 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2023-07-31
 */
@Mapper
public interface OnlineHeavenUserMapper extends BaseMapper<OnlineHeavenUserDo> {

}
