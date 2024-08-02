package com.winterchen.minio.enums;

/**
 * @Author: cc
 * @Description: 文件状态
 * @DateTime: 2022/3/2 9:58
 */
public enum FileState {

    /**
     * 存在且合并
     */
    EXIST_AND_MERGED("存在且合并",0),
    /**
     * 存在未合并
     */
    EXIST_AND_NOT_MERGED("存在未合并",1),
    /**
     * 不存在
     */
    NOT_EXIST("不存在",2);

    Integer flag;

    public Integer getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }

    String name;

    private FileState(String name, int flag) {
        this.name = name;
        this.flag = flag;
    }
}
