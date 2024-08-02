package com.winterchen.minio.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/1 10:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfoVo {
    @TableId
    private Long id;
    private String name;
    private String md5;
    private Boolean merge;
    private Integer chunkTotal;
    private String url;
    private String uploadId;
    private String uuid;
}
