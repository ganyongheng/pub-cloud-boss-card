package rabb.heaven.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 纪念馆
 * </p>
 *
 * @author ganyongheng
 * @since 2024-03-04
 */
@Data
@TableName("online_heaven_shop")
public class OnlineHeavenShopDo extends Model<OnlineHeavenShopDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     *  1 个人  2 （2-3人）3 群体
     */
    private Integer personType;

    /**
     *  1 人 2 动物
     */
    private Integer type;

    /**
     *  丈夫 妻子 父亲 母亲 哥哥
     */
    private Integer roleType;

    /**
     * 是否亲人  1是  0 否
     */
    private Integer isParents;

    /**
     * 是否烈士  1是  0 否
     */
    private Integer isMarty;

    /**
     * 是否社会关注  1是  0 否
     */
    private Integer isSociety;

    /**
     * 标题
     */
    private String title;

    /**
     * 四级地址
     */
    private String address;

    /**
     * 日期格式  1 阳历  2 公历
     */
    private Integer dateType;

    /**
     * 用户id
     */
    private Integer userId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
