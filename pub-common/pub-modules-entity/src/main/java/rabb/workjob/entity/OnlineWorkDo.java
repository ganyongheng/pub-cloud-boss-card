package rabb.workjob.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 工作发布信息
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Data
@TableName("online_work")
public class OnlineWorkDo extends Model<OnlineWorkDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Date createTime;
    /**
     * 兼职  全职  临时工 钟点工  逗号分隔
     */
    private String workType;

    /**
     * 工作四级地址
     */
    private String workAddress;

    /**
     * 工作详细地址
     */
    private String workAddressDetail;

    /**
     * 薪资类型 1 月结  2日结
     */
    private String salaryType;

    /**
     * 薪酬
     */
    private String salary;

    /**
     * 工作内容
     */
    private String workName;


    /**
     * 1热门
     */
    private Integer isHot;

    private String companyName;

    private Integer userId;

    /**
     * 联系手机
     */
    private String phone;

    /**
     * 工作内容描述
     */
    private String msg;

    /**
     * 标题
     */
    private String title;

    /**
     * 联系人
     */
    private String phoneUser;

    private Date updateTime;

    // 1上线  -1下线
    private Integer status;


    // -1 不通过  9 通过  0 提交认证审核
    private Integer identtityStatus;

    @TableField(exist = false)
    private String phoneCode;

    /**
     * 审核不通过原因描述
     */
    private String identtityMsg;

    /**
     * 投递人数
     */
    @TableField(exist = false)
    private long submitCount;
}
