package work.mapper;

import org.apache.ibatis.annotations.Mapper;
import work.entity.OnlineResumeDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 个人简历 Mapper 接口
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Mapper
public interface OnlineResumeMapper extends BaseMapper<OnlineResumeDo> {

}
