package work.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户投递信息
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Data
@TableName("online_work_user")
public class OnlineWorkUserDo extends Model<OnlineWorkUserDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer workId;

    private Integer userId;

    /**
     * 留言
     */
    private String msg;

    /**
     * 联系电话
     */
    private String phone;

    private String userName;

    private Date updateTime;

    private Date createTime;

    /**
     * 简历id
     */
    private Integer resumeId;


}
