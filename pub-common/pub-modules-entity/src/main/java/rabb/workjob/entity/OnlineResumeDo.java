package rabb.workjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 个人简历
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Data
@TableName("online_resume")
public class OnlineResumeDo extends Model<OnlineResumeDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    /**
     * 简历地址
     */
    private String fileUrl;

    private Date updateTime;

    private Date createTime;

    /**
     * 简历名称
     */
    private String resumeName;


}
