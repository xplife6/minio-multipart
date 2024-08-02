package com.winterchen.minio.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/1 10:53
 */
public interface ILocalService {
    String upload(MultipartFile file, String name,String md5, Integer chunkIndex, Integer chunkTotal) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, XmlParserException, InvalidResponseException, InternalException;
}
