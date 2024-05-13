package work.mapper;

import org.apache.ibatis.annotations.Mapper;
import work.entity.OnlineWorkUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 用户投递信息 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Mapper
public interface OnlineWorkUserMapper extends BaseMapper<OnlineWorkUserDo> {

}
