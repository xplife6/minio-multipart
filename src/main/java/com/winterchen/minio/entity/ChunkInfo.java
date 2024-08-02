package com.winterchen.minio.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/1 10:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkInfo {
    @TableId
    private Long id;
    private String uploadId;
    private Integer chunkIndex;
    private String etag;
    private String uuid;
    private Boolean flag;
    private String md5;
    private String url;




}
