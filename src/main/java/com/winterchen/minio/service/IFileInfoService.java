package com.winterchen.minio.service;


import com.winterchen.minio.entity.FileInfoVo;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/2 11:09
 */
public interface IFileInfoService {

    /**
     * 通过id更新
     * @param fileInfo
     * @return
     */
    public Integer updateById(FileInfoVo fileInfo);

    /**
     * 插入
     * @param fileInfo
     * @return
     */
    public Integer insert(FileInfoVo fileInfo);
    public Integer updateFile(FileInfoVo fileInfo);
}
