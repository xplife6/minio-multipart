package com.winterchen.minio.config;

import com.google.common.collect.Multimap;
import io.minio.CreateMultipartUploadResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.UploadPartResponse;
import io.minio.errors.*;
import io.minio.messages.Part;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MyMinioClient extends MinioClient {


    public MyMinioClient(MinioClient client) {
        super(client);
    }



    public CreateMultipartUploadResponse createMultipartUpload(String bucketName, String objectName,Multimap<String, String> headers) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return super.createMultipartUpload(bucketName, null, objectName, headers, null);
    }
//
//    @Override
//    public UploadPartResponse uploadPart(String bucketName, String region, String objectName, Object data, int length, String uploadId, int partNumber, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
//        return super.uploadPart(bucketName, region, objectName, data, length, uploadId, partNumber, extraHeaders, extraQueryParams);
//    }

    @Override
    public ObjectWriteResponse completeMultipartUpload(String bucketName, String region, String objectName, String uploadId, Part[] parts, Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) throws NoSuchAlgorithmException, InsufficientDataException, IOException, InvalidKeyException, ServerException, XmlParserException, ErrorResponseException, InternalException, InvalidResponseException {
        return super.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }



}
