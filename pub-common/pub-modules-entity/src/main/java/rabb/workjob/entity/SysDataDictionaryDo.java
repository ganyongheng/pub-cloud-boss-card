package rabb.workjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 系统数据字典数据库
 * </p>
 *
 * @author ganyongheng
 * @since 2023-08-01
 */
@Data
@TableName("sys_data_dictionary")
public class SysDataDictionaryDo extends Model<SysDataDictionaryDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String paramName;

    private String paramKey;

    private String paramValue;

    private String paramDesc;

    /**
     * -1 删除 其它 未删除
     */
    private Integer status;

    private Date createDate;


}
