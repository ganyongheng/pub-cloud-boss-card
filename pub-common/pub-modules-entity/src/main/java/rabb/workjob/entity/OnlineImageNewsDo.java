package rabb.workjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 轮播图
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-24
 */
@Data
@TableName("online_image_news")
public class OnlineImageNewsDo extends Model<OnlineImageNewsDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String imageUrl;

    private Date createTime;

    /**
     * -1  下线   9上线 
     */
    private Integer status;



}
