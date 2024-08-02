package com.winterchen.minio.service.impl;

import com.winterchen.minio.entity.FileInfoVo;
import com.winterchen.minio.mapper.FileInfoMapper;
import com.winterchen.minio.service.IFileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/2 11:09
 */
@Service
public class FileInfoServiceImpl implements IFileInfoService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    /**
     * 通过id更新
     * @param fileInfo
     * @return
     */
    @Override
    public Integer updateById(FileInfoVo fileInfo){
        return fileInfoMapper.updateById(fileInfo);
    }
    @Override
    public Integer insert(FileInfoVo fileInfo){
        return fileInfoMapper.insert(fileInfo);
    }

    @Override
    public Integer updateFile(FileInfoVo fileInfo) {
        return fileInfoMapper.updateById(fileInfo);
    }
}
