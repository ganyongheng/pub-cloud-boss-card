package rabb.heaven.entity;

import com.alibaba.fastjson.annotation.JSONField;
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

    private Integer id;

    private String name;

    /**
     * 生日
     */
    private LocalDateTime birthday;

    private LocalDateTime deathday;

    /**
     * 宗教信仰
     */
    private Integer religionType;

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
    private Integer eventType;

    /**
     * 事发日期
     */
    private String eventDay;

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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDateTime birthday) {
        this.birthday = birthday;
    }

    public LocalDateTime getDeathday() {
        return deathday;
    }

    public void setDeathday(LocalDateTime deathday) {
        this.deathday = deathday;
    }

    public Integer getReligionType() {
        return religionType;
    }

    public void setReligionType(Integer religionType) {
        this.religionType = religionType;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getDeathAddress() {
        return deathAddress;
    }

    public void setDeathAddress(String deathAddress) {
        this.deathAddress = deathAddress;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getEventDay() {
        return eventDay;
    }

    public void setEventDay(String eventDay) {
        this.eventDay = eventDay;
    }

    public Integer getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(Integer deathCount) {
        this.deathCount = deathCount;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getTextMsg() {
        return textMsg;
    }

    public void setTextMsg(String textMsg) {
        this.textMsg = textMsg;
    }

    public String getAnimalType() {
        return animalType;
    }

    public void setAnimalType(String animalType) {
        this.animalType = animalType;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
