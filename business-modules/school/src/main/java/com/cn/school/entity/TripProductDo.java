package com.cn.school.entity;

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
 * 班次表
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-16
 */
@Data
@TableName("trip_product")
public class TripProductDo extends Model<TripProductDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 航线id
     */
    private Integer tripAreaId;

    /**
     * 费用
     */
    private String fee;

    private Date createTime;


    private Integer deleteStatus;

}