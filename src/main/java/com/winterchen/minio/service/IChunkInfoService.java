package com.winterchen.minio.service;


import com.winterchen.minio.entity.ChunkInfo;

import java.util.List;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/2 10:47
 */
public interface IChunkInfoService {
    /**
     * 删除
     * @param id
     * @return
     */
    public Integer deleteById(String id);

    /**
     * 通过uploadId删除
     * @param uploadId
     * @return
     */
    public Integer deleteByUploadId(String uploadId);

    /**
     * 根据uploadId 和chunkIndex 查询
     * @param uploadId
     * @param chunkIndex
     * @return
     */
    public ChunkInfo checkChunk(String uploadId, Integer chunkIndex);

    /**
     * 更新
     * @param id
     * @return
     */
    public Integer updateById(String id);

    /**
     * 插入
     * @param chunkInfo
     * @return
     */
    public Integer insert(ChunkInfo chunkInfo);

    /**
     * 查询列表
     * @param uploadId
     * @return
     */
    public List<ChunkInfo> list(String uploadId);

    public int insertList(List<ChunkInfo> chunkInfos);

    public int  updateInfo(ChunkInfo chunkInfo);
}
