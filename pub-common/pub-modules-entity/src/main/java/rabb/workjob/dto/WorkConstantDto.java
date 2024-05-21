package rabb.workjob.dto;

public class WorkConstantDto {


    /**
     *  0 初始化  1 实名认证
     */
    public interface IdentityStatus{
        public static final Integer no = 0;
        public static final Integer yes = 1;
    }
    /**
     *1 企业  2 个人
     */
    public interface RoleType{
        public static final Integer person = 2;
        public static final Integer company = 1;
        public static final Integer no = 0;
    }
    /**
     * 是否黑名單  0 黑名单 9 否
     */
    public interface DeleteStatus{
        public static final Integer yes = 0;
        public static final Integer no = 9;
    }
    /**
     * 认证状态 0 初始化  -1 不通过  9 通过
     */
    public interface IdenttityStatus{
        public static final Integer yes = 9;
        public static final Integer no = -1;
        public static final Integer submit = 1;
        public static final Integer submitCompany = 2;
        public static final Integer initialize = 0;
    }
    /**
     * 发布状态 1上线  -1下线
     */
    public interface WorkStatus{
        public static final Integer yes = 1;
        public static final Integer no = -1;
    }
    /**
     * 认证状态   -1 不通过  9 通过  0 提交认证审核
     */
    public interface WorkIdenttityStatus{
        public static final Integer yes = 9;
        public static final Integer no = -1;
        public static final Integer submit = 0;
    }
    /**
     * 热门   1热门
     */
    public interface isHot{
        public static final Integer yes = 1;
        public static final Integer no = 0;
    }
    /**
     * 0 未绑定  1 已绑定
     */
    public interface phoneAble{
        public static final Integer yes = 1;
        public static final Integer no = 0;
    }



}
