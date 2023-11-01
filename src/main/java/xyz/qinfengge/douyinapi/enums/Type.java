package xyz.qinfengge.douyinapi.enums;

import lombok.Getter;

/**
 * @author lza
 * @date 2023/10/11-17:28
 **/
@Getter
public enum Type {

    /**
     * 用户上传的视频
     */
    POST(1, "post"),

    /**
     * 用户的喜欢
     */
    LIKE(2, "like"),

    /**
     * 用户的收藏
     */
    COLLECTION(3, "collection");

    private final Integer code;
    private final String flag;

    Type(Integer code, String flag) {
        this.code = code;
        this.flag = flag;
    }

    public static Integer getTypeCode(String flag) {
        for (Type type : Type.values()) {
            if (type.flag.equals(flag)) {
                return type.code;
            }
        }
        throw new IllegalArgumentException("Invalid flag: " + flag);
    }

}
