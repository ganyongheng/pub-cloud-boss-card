package work.mapper;

import org.mapstruct.Mapper;
import work.entity.OnlineWorkDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 工作发布信息 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Mapper
public interface OnlineWorkMapper extends BaseMapper<OnlineWorkDo> {

}
