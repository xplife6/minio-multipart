package com.winterchen.minio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winterchen.minio.entity.ChunkInfo;
import com.winterchen.minio.mapper.ChunkInfoMapper;
import com.winterchen.minio.service.IChunkInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/2 10:47
 */
@Service
public class ChunkInfoServiceImpl implements IChunkInfoService {

    @Autowired
    private ChunkInfoMapper chunkInfoMapper;


    @Override
    public Integer deleteById(String id){
        return chunkInfoMapper.deleteById(id);
    }

    @Override
    public Integer deleteByUploadId(String uploadId){
        LambdaQueryWrapper<ChunkInfo> wrapper = new LambdaQueryWrapper();
        wrapper.eq(ChunkInfo::getUploadId,uploadId);
        return chunkInfoMapper.delete(wrapper);
    }

    /**
     * 根据uploadId 和chunkIndex 查询
     * @param uploadId
     * @param chunkIndex
     * @return
     */
    @Override
    public ChunkInfo checkChunk(String uploadId,Integer chunkIndex){
        LambdaQueryWrapper<ChunkInfo> lambda = new QueryWrapper<ChunkInfo>().lambda();
        lambda.eq(ChunkInfo::getUploadId,uploadId);
        lambda.eq(ChunkInfo::getChunkIndex,chunkIndex);
        ChunkInfo chunkInfo = chunkInfoMapper.selectOne(lambda);
        return chunkInfo;
    }

    /**
     * 更新
     * @param id
     * @return
     */
    @Override
    public Integer updateById(String id) {
        return null;
    }

    /**
     * 插入
     * @param chunkInfo
     * @return
     */
    @Override
    public Integer insert(ChunkInfo chunkInfo) {
        return chunkInfoMapper.insert(chunkInfo);
    }

    /**
     * 列表
     * @param uploadId
     * @return
     */
    @Override
    public List<ChunkInfo> list(String uploadId){
        LambdaQueryWrapper<ChunkInfo> lambda = new QueryWrapper<ChunkInfo>().lambda();
        lambda.eq(ChunkInfo::getUploadId,uploadId);
        lambda.orderByAsc(ChunkInfo::getChunkIndex);
        return chunkInfoMapper.selectList(lambda);
    }

    @Override
    public int insertList(List<ChunkInfo> chunkInfos) {
        if (chunkInfos.size()>0){
            chunkInfos.forEach(t->chunkInfoMapper.insert(t));
        }
        return 0;
    }

    @Override
    public int updateInfo(ChunkInfo chunkInfo) {
        ChunkInfo chunk = new ChunkInfo();
        chunk.setMd5(chunkInfo.getMd5());
        chunk.setUploadId(chunkInfo.getUploadId());
        chunk.setChunkIndex(chunkInfo.getChunkIndex());
        QueryWrapper<ChunkInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",chunkInfo.getMd5()).eq("chunk_index",chunkInfo.getChunkIndex())
                .eq("upload_id",chunkInfo.getUploadId());
        ChunkInfo info = chunkInfoMapper.selectOne(queryWrapper);
        if (info!=null){
            info.setFlag(true);
            int i = chunkInfoMapper.updateById(info);
            return i;
        }

        return 0;
    }


}
