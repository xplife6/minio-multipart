package com.winterchen.minio.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: cc
 * @Description: TODO
 * @DateTime: 2022/3/2 9:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileResult {
    private String message;
}
