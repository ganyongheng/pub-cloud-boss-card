package rabb.heaven.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 纪念详细
 * </p>
 *
 * @author ganyongheng
 * @since 2024-03-04
 */
@Data
@TableName("online_heaven_shop_detail")
public class OnlineHeavenShopDetailDo extends Model<OnlineHeavenShopDetailDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String nikeName;

    /**
     * 生日
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deathday;

    /**
     * 宗教信仰
     */
    private String religion;

    /**
     * 职业
     */
    private String career;

    /**
     * 1 男  2 女
     */
    private Integer sex;

    private String deathAddress;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事发日期
     */
    @JSONField(format = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date eventDay;

    /**
     * 人数
     */
    private Integer deathCount;

    private Integer minAge;

    private Integer maxAge;

    /**
     * 事件描述
     */
    private String textMsg;

    /**
     * 动物类型
     */
    private String animalType;

    /**
     * 关联纪念馆id
     */
    private Integer parentId;



    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
