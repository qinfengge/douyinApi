package xyz.qinfengge.douyinapi.enums;

import lombok.Getter;

/**
 * @author lza
 * @date 2023/10/11-17:28
 **/
@Getter
public enum Type {

    /**
     * 视频
     */
    VIDEO(1, "视频"),

    /**
     * 图集
     */
    ALBUM(2, "图集");


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
