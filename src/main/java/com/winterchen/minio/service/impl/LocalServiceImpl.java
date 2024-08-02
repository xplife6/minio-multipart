package com.winterchen.minio.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.winterchen.minio.entity.ChunkInfo;
import com.winterchen.minio.entity.FileInfoVo;
import com.winterchen.minio.enums.FileState;
import com.winterchen.minio.mapper.ChunkInfoMapper;
import com.winterchen.minio.mapper.FileInfoMapper;
import com.winterchen.minio.service.IChunkInfoService;
import com.winterchen.minio.service.IFileInfoService;
import com.winterchen.minio.service.ILocalService;
import com.winterchen.minio.util.MinioUtil;
import com.google.common.collect.HashMultimap;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.messages.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/1 10:54
 */
@Service
public class LocalServiceImpl implements ILocalService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private ChunkInfoMapper chunkInfoMapper;

    @Autowired
    private IChunkInfoService chunkInfoService;

    @Autowired
    private IFileInfoService fileInfoService;



    @Override
    public String upload(MultipartFile file, String name, String md5, Integer chunkIndex, Integer chunkTotal) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {

        //创建args
        PutObjectArgs args = getPutObjectArgs(file,name);
        HashMultimap<String, String> headers = HashMultimap.create();
        headers.put("Content-Type","application/octet-stream");



        FileInfoVo info = selectByMD5(md5);
        //获取uploadId
        uploadIdStrategy(info, file, name, md5, chunkTotal);
        //判断是否已合并
        if(info.getMerge()){
            return "秒传！";
        }
        //查询分片是否存在 存在则删除分片
        ChunkInfo chunkInfo = chunkInfoService.checkChunk(info.getUploadId(), chunkIndex);
//        if(!Objects.isNull(chunkInfo)){
//            chunkInfoService.deleteById(chunkInfo.getId());
//        }

        //上传分片 并保存数据库
//        ChunkInfo uploadChunkInfo = uploadChunk(args, info.getUploadId(), chunkIndex, headers);
//        Integer i = chunkInfoService.insert(uploadChunkInfo);
//
//        // 判断是否最后一块分片
//        if(chunkIndex.equals(chunkTotal)){
//            List<ChunkInfo> list = chunkInfoService.list(info.getUploadId());
//            String url  = merge(list, args, info.getUploadId());
//            //合并成功则更新数据库
//            if(!Objects.isNull(url)){
//                info.setMerge(true);
//                info.setUrl(url);
//                fileInfoService.updateById(info);
//                Integer integer = chunkInfoService.deleteByUploadId(info.getUploadId());
//                return url;
//            }else {
//                return "合并失败";
//            }
//
//        }
        return info.getUploadId();

    }

    public PutObjectArgs getPutObjectArgs(MultipartFile file,String name) throws IOException {
        InputStream stream = file.getInputStream();
        return PutObjectArgs.builder()
                .object(name)
                .bucket(MinioUtil.BUCKET_NAME)
                .contentType("application/octet-stream")
                .stream(stream, stream.available(), MinioUtil.PART_MAX_SIZE).build();
    }


    public String merge(PutObjectArgs args ,String uploadId,String md5) throws ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        LambdaQueryWrapper<ChunkInfo> lambda = new QueryWrapper<ChunkInfo>().lambda();
        lambda.eq(ChunkInfo::getUploadId,uploadId);
        lambda.orderByAsc(ChunkInfo::getChunkIndex);
        List<ChunkInfo> fileInfos = chunkInfoMapper.selectList(lambda);

        Part[] parts = new Part[fileInfos.size()];
        for (ChunkInfo fileInfo : fileInfos) {
            parts[fileInfo.getChunkIndex()-1] = new Part(fileInfo.getChunkIndex(),fileInfo.getEtag());
        }
        MinioUtil.completeMultipartUpload(args, uploadId, parts);
        return "合并完成";
    }


    /**
     * 通过MD5查询FileInfo
     * @param md5 MD5
     * @return FileInfo
     */
    private FileInfoVo selectByMD5(String md5){
        LambdaQueryWrapper<FileInfoVo> lambda = new QueryWrapper<FileInfoVo>().lambda();
        lambda.eq(FileInfoVo::getMd5,md5);
        FileInfoVo fileInfo = fileInfoMapper.selectOne(lambda);
        return Objects.isNull(fileInfo)? new FileInfoVo():fileInfo;
    }

    private Integer checkMerge(FileInfoVo fileInfo){
        //存在
        if(Objects.isNull(fileInfo.getUploadId())){
            //已合并
            if(fileInfo.getMerge()){
                return FileState.EXIST_AND_MERGED.getFlag();
            }else {
                return FileState.EXIST_AND_NOT_MERGED.getFlag();
            }
        }
        return FileState.NOT_EXIST.getFlag();
    }

    /**
     * 创建fileInfo
     */
    private FileInfoVo uploadIdStrategy(FileInfoVo fileInfo, MultipartFile file, String name, String md5, Integer chunkTotal) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {

        //存在
        if(Objects.isNull(fileInfo.getUploadId())){
            PutObjectArgs args = getPutObjectArgs(file,name);
            HashMultimap<String, String> headers = HashMultimap.create();
            headers.put("Content-Type","application/octet-stream");
            String uploadId = MinioUtil.createUploadId(args, headers);
            fileInfo.setUploadId(uploadId);
            String id = UUID.randomUUID().toString().replaceAll("-", "");
//            fileInfo.setId(id);
            fileInfo.setMd5(md5);
            fileInfo.setChunkTotal(chunkTotal);
            fileInfo.setMerge(false);
            fileInfo.setName(name);
            fileInfoService.insert(fileInfo);
        }
        return fileInfo;


    }


//    /**
//     * 上传分片
//     * @param args
//     * @param uploadId
//     * @param chunkIndex
//     * @param headers
//     * @return
//     */
//    private ChunkInfo uploadChunk(PutObjectArgs args, String uploadId, Integer chunkIndex,HashMultimap<String, String> headers) throws ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
//        String etag = MinioUtil.uploadPart(args, args.stream(), args.objectSize(), uploadId, chunkIndex, headers);
//        return ChunkInfo.builder().chunkIndex(chunkIndex).uploadId(uploadId).etga(etag).id(UUID.randomUUID().toString().replaceAll("-","")).build();
//    }

    private String merge(List<ChunkInfo> chunkInfos,PutObjectArgs args,String uploadId) throws ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, IOException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException {
        Part[] parts = new Part[chunkInfos.size()];
        for (ChunkInfo chunkInfo : chunkInfos) {
            parts[chunkInfo.getChunkIndex()-1] = new Part(chunkInfo.getChunkIndex(),chunkInfo.getEtag());
        }
        return MinioUtil.completeMultipartUpload(args, uploadId, parts).region();
    }
}
