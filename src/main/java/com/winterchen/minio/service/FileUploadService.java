package com.winterchen.minio.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.javafx.tk.TKPulseListener;
import com.winterchen.minio.base.MultipartUploadCreate;
import com.winterchen.minio.base.ResultCode;
import com.winterchen.minio.entity.ChunkInfo;
import com.winterchen.minio.entity.FileInfoVo;
import com.winterchen.minio.exception.BusinessException;
import com.winterchen.minio.mapper.ChunkInfoMapper;
import com.winterchen.minio.mapper.FileInfoMapper;
import com.winterchen.minio.request.CompleteMultipartUploadRequest;
import com.winterchen.minio.request.MultipartUploadCreateRequest;
import com.winterchen.minio.response.FileUploadResponse;
import com.winterchen.minio.response.MultipartUploadCreateResponse;
import com.winterchen.minio.utils.MinioHelper;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.ServerException;
import io.minio.messages.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.management.Query;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author winterchen
 * @version 1.0
 * @date 2022/4/12 14:24
 * @description 文件上传服务
 **/
@Slf4j
@Service
public class FileUploadService {

    @Autowired
    private ChunkInfoMapper chunkInfoMapper;
    @Autowired
    private FileInfoMapper fileInfoMapper;

    private final MinioHelper minioHelper;

    public FileUploadService(MinioHelper minioHelper) {
        this.minioHelper = minioHelper;
    }


    /**
     * 普通上传
     * @param file
     * @return
     */
    public FileUploadResponse upload(MultipartFile file) {
        Assert.notNull(file, "文件不能为空");
        log.info("start file upload");

        //文件上传
        try {
            return minioHelper.uploadFile(file);
        } catch (IOException e) {
            log.error("file upload error.", e);
            throw BusinessException.newBusinessException(ResultCode.FILE_IO_ERROR.getCode());
        } catch (ServerException e) {
            log.error("minio server error.", e);
            throw BusinessException.newBusinessException(ResultCode.MINIO_SERVER_ERROR.getCode());
        } catch (InsufficientDataException e) {
            log.error("insufficient data throw exception", e);
            throw BusinessException.newBusinessException(ResultCode.MINIO_INSUFFICIENT_DATA.getCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.newBusinessException(ResultCode.KNOWN_ERROR.getCode());
        }


    }


    /**
     * 创建分片上传
     * @param createRequest
     * @return
     */
    public MultipartUploadCreateResponse createMultipartUpload(MultipartUploadCreateRequest createRequest) {
        log.info("创建分片上传开始, createRequest: [{}]", createRequest);
        String uuid = UUID.randomUUID().toString();
        MultipartUploadCreateResponse response = new MultipartUploadCreateResponse();
        response.setChunks(new LinkedList<>());
        response.setUuid(uuid);

        QueryWrapper<FileInfoVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",createRequest.getMd5()).eq("merge",true);
        List<FileInfoVo> fileInfoVos = fileInfoMapper.selectList(queryWrapper);
        //已上传过本文件,走秒传；
        if (fileInfoVos!=null && fileInfoVos.size()>0){
            FileInfoVo fileInfoVo = fileInfoVos.get(0);
            FileInfoVo infoVo = new FileInfoVo();
            infoVo.setMd5(createRequest.getMd5());
            infoVo.setUuid(uuid);
            infoVo.setMerge(true);
            infoVo.setName(createRequest.getFileName());
            infoVo.setChunkTotal(response.getChunks().size());
            infoVo.setUrl(fileInfoVo.getUrl());
            fileInfoMapper.insert(infoVo);
            response.setCode(101);
            return response;
        }



        QueryWrapper<FileInfoVo> wrapper = new QueryWrapper<>();
        wrapper.eq("md5",createRequest.getMd5()).eq("merge",false);
        List<FileInfoVo> fileVos = fileInfoMapper.selectList(wrapper);
        List<ChunkInfo> uploadChunks = new ArrayList<>();
        List<ChunkInfo> unUploadChunks = new ArrayList<>();
        String origUploadId = "";
        //获取上传未上传成功的分片列表
        if (fileVos!=null && fileVos.size()>0){
            FileInfoVo fileInfoVo = fileVos.get(0);
            QueryWrapper<ChunkInfo> infoQueryWrapper = new QueryWrapper<>();
            infoQueryWrapper.eq("md5",fileInfoVo.getMd5());
            uploadChunks = chunkInfoMapper.selectList(infoQueryWrapper);
            if(uploadChunks!=null && uploadChunks.size()>0){
                ChunkInfo chunkInfo = uploadChunks.get(0);
                origUploadId= chunkInfo.getUploadId();
            }
        }


        final MultipartUploadCreate uploadCreate = MultipartUploadCreate.builder()
                .bucketName(minioHelper.minioProperties.getBucketName())
                .objectName(createRequest.getFileName())
//                .objectName(createRequest.getMd5())
                .build();
        final CreateMultipartUploadResponse uploadId = minioHelper.uploadId(uploadCreate);

        uploadCreate.setUploadId(uploadId.result().uploadId());
        response.setUploadId(uploadCreate.getUploadId());
        Map<String, String> reqParams = new HashMap<>();


          reqParams.put("uploadId", uploadId.result().uploadId());


//        for (int i = 0; i <= createRequest.getChunkSize(); i++) {
        for (int i = 0; i <= createRequest.getChunkSize(); i++) {
            MultipartUploadCreateResponse.UploadCreateItem item = new MultipartUploadCreateResponse.UploadCreateItem();
//            reqParams.put("partNumber", String.valueOf(i+1));
//            String presignedObjectUrl = minioHelper.getPresignedObjectUrl(uploadCreate.getBucketName(), uploadCreate.getObjectName(), reqParams);
//            if (StringUtils.isNotBlank(minioHelper.minioProperties.getPath())) {//如果线上环境配置了域名解析，可以进行替换
//                presignedObjectUrl = presignedObjectUrl.replace(minioHelper.minioProperties.getEndpoint(), minioHelper.minioProperties.getPath());
//            }
//            item.setPartNumber(i);
//            item.setUploadUrl(presignedObjectUrl);

//            response.getChunks().add(item);

            //检测到未上传完成整的文件，则要补充上传分片
            if(uploadChunks!=null && uploadChunks.size()>0){
                for (ChunkInfo uploadChunk : uploadChunks) {
                    if (uploadChunk.getChunkIndex().equals(i)){
                        item.setUploadUrl(uploadChunk.getUrl());
                        item.setFlag(uploadChunk.getFlag());
                        item.setPartNumber(i);
                        response.getChunks().add(item);
                    }
                }
                //将原uploadId返回至前端，以便合并文件
                if (origUploadId!=null && origUploadId!=""){
                    response.setUploadId(origUploadId);
                }
                response.setCode(301);
            }else {
                reqParams.put("partNumber", String.valueOf(i+1));
                String presignedObjectUrl = minioHelper.getPresignedObjectUrl(uploadCreate.getBucketName(), uploadCreate.getObjectName(), reqParams);
                if (StringUtils.isNotBlank(minioHelper.minioProperties.getPath())) {//如果线上环境配置了域名解析，可以进行替换
                    presignedObjectUrl = presignedObjectUrl.replace(minioHelper.minioProperties.getEndpoint(), minioHelper.minioProperties.getPath());
                }
                item.setPartNumber(i);
                item.setUploadUrl(presignedObjectUrl);
                item.setFlag(false);

                ChunkInfo chunkInfo = new ChunkInfo();
                chunkInfo.setUploadId(uploadId.result().uploadId());
                chunkInfo.setChunkIndex(i);
                chunkInfo.setUuid(uuid);
                chunkInfo.setFlag(false);
                chunkInfo.setMd5(createRequest.getMd5());
                chunkInfo.setUrl(presignedObjectUrl);
                chunkInfoMapper.insert(chunkInfo);
                response.getChunks().add(item);
                response.setCode(201);
            }
        }
        //新建文件上传信息
        FileInfoVo fileInfoVo = new FileInfoVo();
        fileInfoVo.setMd5(createRequest.getMd5());
        fileInfoVo.setUploadId(uploadId.result().uploadId());
        fileInfoVo.setMerge(false);
        fileInfoVo.setName(createRequest.getFileName());
        fileInfoVo.setChunkTotal(response.getChunks().size());
        fileInfoVo.setUuid(uuid);
        fileInfoMapper.insert(fileInfoVo);

        log.info("创建分片上传结束, createRequest: [{}]", createRequest);
        return response;
    }

    /**
     * 分片合并
     * @param uploadRequest
     */
    public FileUploadResponse completeMultipartUpload(CompleteMultipartUploadRequest uploadRequest) {
        log.info("文件合并开始, uploadRequest: [{}]", uploadRequest);
        String now = String.valueOf(System.currentTimeMillis());
        try {
            final ListPartsResponse listMultipart = minioHelper.listMultipart(MultipartUploadCreate.builder()
                    .bucketName(minioHelper.minioProperties.getBucketName())
                    .objectName(uploadRequest.getFileName())
                    .maxParts(uploadRequest.getChunkSize() + 10)
                    .uploadId(uploadRequest.getUploadId())
                    .partNumberMarker(1)
                    .build());
            final ObjectWriteResponse objectWriteResponse = minioHelper.completeMultipartUpload(MultipartUploadCreate.builder()
                    .bucketName(minioHelper.minioProperties.getBucketName())
                    .uploadId(uploadRequest.getUploadId())
                    .objectName(uploadRequest.getFileName())
                    .parts(listMultipart.result().partList().toArray(new Part[]{}))
                    .build());

            String url= minioHelper.minioProperties.getDownloadUri() + "/" + minioHelper.minioProperties.getBucketName() + "/" + uploadRequest.getFileName();
            //获取
            QueryWrapper<FileInfoVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid",uploadRequest.getUuid());
            FileInfoVo fileInfoVo = fileInfoMapper.selectOne(queryWrapper);
            fileInfoVo.setMerge(true);
            fileInfoVo.setUrl(url);
            fileInfoMapper.updateById(fileInfoVo);

            log.info("文件合并结束, uploadRequest: [{}]", uploadRequest);
            return FileUploadResponse.builder()
                    .url(minioHelper.minioProperties.getDownloadUri() + "/" + minioHelper.minioProperties.getBucketName() + "/" + uploadRequest.getFileName())
                    .build();
        } catch (Exception e) {
            log.error("合并分片失败", e);
        }
        log.info("文件合并结束, uploadRequest: [{}]", uploadRequest);
        return null;
    }


    public void remove(String fileName) {
        if (StringUtils.isBlank(fileName)) return;
        log.info("删除文件开始, fileName: [{}]",fileName);
        try {
            minioHelper.removeFile(fileName);
        } catch (Exception e) {
            log.error("删除文件失败", e);
        }
        log.info("删除文件结束, fileName: [{}]",fileName);
    }


    public void uploadlist (CompleteMultipartUploadRequest uploadRequest){

        final ListPartsResponse listMultipart = minioHelper.listMultipart(MultipartUploadCreate.builder()
                .bucketName(minioHelper.minioProperties.getBucketName())
                .objectName(uploadRequest.getFileName())
                .maxParts(uploadRequest.getChunkSize() + 10)
                .uploadId(uploadRequest.getUploadId())
                .partNumberMarker(1)
                .build());
        log.info("获取列表文件，listMultipart[{}]",listMultipart);
    }
}