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

    private LocalDateTime createTime;

    private Date startTime;

    private Date endTime;

    /**
     * 1：兼职  2：全职 3：临时工 4钟点工 
     */
    private Integer workType;

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
    private Integer salaryType;

    /**
     * 薪酬
     */
    private String salary;

    /**
     * 工作内容
     */
    private String workName;

    /**
     * 0 初始化  1审核通过 -1 审核不通过
     */
    private Integer checkStatus;

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

    //0 初始化  1上线  -1下线
    private Integer status;

}
