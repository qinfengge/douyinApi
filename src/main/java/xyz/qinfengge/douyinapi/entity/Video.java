package xyz.qinfengge.douyinapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName video
 */
@TableName(value ="video")
@Data
public class Video implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;


    private String name;
    /**
     * 
     */
    private String url;

    /**
     * 
     */
    private Integer count;

    /**
     * 
     */
    private Integer heart;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}