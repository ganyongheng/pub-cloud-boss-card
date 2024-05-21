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
 * 用户认证历史记录表
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-21
 */
@Data
@TableName("online_user_identity_history")
public class OnlineUserIdentityHistoryDo extends Model<OnlineUserIdentityHistoryDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Date createTime;

    /**
     * 真实姓名
     */
    private String identityName;

    /**
     * 1 企业  2 个人 
     */
    private Integer roleId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 身份证正面照
     */
    private String identityImageUpUrl;

    /**
     * 身份证反面照
     */
    private String identityImageDownUrl;

    /**
     * 营业执照
     */
    private String businessImageUrl;

    /**
     * 企业编码唯一
     */
    private String businessCode;

    private Date updateTime;

    /**
     * 认证状态 -1 不通过  9 通过  1 提交认证审核  2 提交企业认证
     */
    private String identtityStatus;

    /**
     * 身份证号码
     */
    private String identityNumber;


}
