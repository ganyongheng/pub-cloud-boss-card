package offline.work.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import rabb.workjob.dto.WorkConstantDto;
import rabb.workjob.entity.OnlineImageNewsDo;
import rabb.workjob.mapper.OnlineImageNewsMapper;
import rabb.workjob.service.IOnlineImageNewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 轮播图 服务实现类
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-24
 */
@Service
public class OnlineImageNewsServiceImpl extends ServiceImpl<OnlineImageNewsMapper, OnlineImageNewsDo> implements IOnlineImageNewsService {

    public void addImage(JSONArray jsonArray) {
        if(!jsonArray.isEmpty()){
            List<OnlineImageNewsDo> list = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject json = JSON.parseObject(JSON.toJSONString(o));
                OnlineImageNewsDo onlineImageNewsDo=new OnlineImageNewsDo();
                onlineImageNewsDo.setCreateTime(new Date());
                onlineImageNewsDo.setStatus(WorkConstantDto.imageAble.yes);
                onlineImageNewsDo.setImageUrl(json.getString("url"));
                list.add(onlineImageNewsDo);
            }
            saveBatch(list);
        }
    }

    public void upOrDowm(OnlineImageNewsDo onlineImageNewsDo) {
        updateById(onlineImageNewsDo);
    }
}
