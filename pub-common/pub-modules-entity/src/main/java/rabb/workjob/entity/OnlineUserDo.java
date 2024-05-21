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
 * 用户信息
 * </p>
 *
 * @author ganyongheng
 * @since 2024-05-12
 */
@Data
@TableName("online_user")
public class OnlineUserDo extends Model<OnlineUserDo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private String openid;

    private String wxunionid;

    /**
     * 是否黑名單  0 黑名单 9 否
     */
    private Integer isDelete;

    private String nickName;

    private Date createTime;

    /**
     * 头像
     */
    private String imageUrl;

    /**
     * 真实姓名
     */
    private String identityName;

    private String phone;

    /**
     * 1 企业  2 个人  0 未实名认证
     */
    private Integer roleId;

    private Integer vipLevel;

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
     * 认证状态 0 初始化  -1 不通过  9 通过  1 提交认证审核
     */
    private Integer identtityStatus;

    /**
     * 短信验证码
     */
    @TableField(exist = false)
    private String phoneCode;

    /**
     * 身份证号码
     */
    private  String identityNumber;

    /**
     * 是否绑定手机 0 未绑定  1已绑定
     */
    private Integer phoneAble;

}
