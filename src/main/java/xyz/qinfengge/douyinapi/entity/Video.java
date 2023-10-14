package xyz.qinfengge.douyinapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 
 * @TableName video
 */
@TableName(value ="video", autoResultMap = true)
@Data
public class Video implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;


    private String name;
    /**
     * 视频URL
     */
    private String url;

    /**
     * 缩略图URL
     */
    private String thumbnail;
    /**
     * 作者/来源账号
     */
    private String userName;

    /**
     * 视频类型
     * 1：post, 2: like, 3: collection
     */
    private Integer type;

    /**
     * 标签列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 图集
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /**
     * 视频原声
     */
    private String audio;

    /**
     * 视频日期
     */
    private String created;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}