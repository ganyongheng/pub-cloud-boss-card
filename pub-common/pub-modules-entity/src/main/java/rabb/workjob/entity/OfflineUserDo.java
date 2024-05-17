package rabb.workjob.entity;

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
 * 离线用户
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-16
 */
@Data
@TableName("offline_user")
public class OfflineUserDo extends Model<OfflineUserDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String pwd;

    private Date createTime;

    private Integer roleId;

    private Date updateTime;


}
