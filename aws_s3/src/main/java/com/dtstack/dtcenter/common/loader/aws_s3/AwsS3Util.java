package com.dtstack.dtcenter.common.loader.aws_s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.dtstack.dtcenter.loader.dto.source.AwsS3SourceDTO;
import com.dtstack.dtcenter.loader.dto.source.ISourceDTO;
import com.dtstack.dtcenter.loader.exception.DtLoaderException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * aws s3 工具类
 *
 * @author ：wangchuan
 * date：Created in 上午10:07 2021/5/6
 * company: www.dtstack.com
 */
public class AwsS3Util {

    private static final Integer TIMEOUT = 60 * 1000;

    /**
     * 获取 aws s3 客户端
     *
     * @param sourceDTO 数据源信息
     * @return aws s3客户端
     */
    public static AmazonS3 getClient(AwsS3SourceDTO sourceDTO) {
        String region = StringUtils.isNotBlank(sourceDTO.getRegion()) ? sourceDTO.getRegion() : Regions.CN_NORTH_1.getName();
        BasicAWSCredentials credentials = new BasicAWSCredentials(sourceDTO.getAccessKey(), sourceDTO.getSecretKey());
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setRequestTimeout(TIMEOUT);
        configuration.setClientExecutionTimeout(TIMEOUT);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                // 设置服务器所属地区
                .withRegion(region)
                .withClientConfiguration(configuration).build();
    }

    /**
     * 关闭 aws s3
     *
     * @param amazonS3 aws s3客户端
     */
    public static void closeAmazonS3(AmazonS3 amazonS3) {
        if (Objects.nonNull(amazonS3)) {
            amazonS3.shutdown();
        }
    }

    /**
     * 强转 sourceDTO 为 AwsS3SourceDTO
     *
     * @param sourceDTO aws s3 sourceDTO
     * @return 转换后的 aws s3 sourceDTO
     */
    public static AwsS3SourceDTO convertSourceDTO(ISourceDTO sourceDTO) {
        if (!(sourceDTO instanceof AwsS3SourceDTO)) {
            throw new DtLoaderException("please pass in AwsS3SourceDTO...");
        }
        return (AwsS3SourceDTO) sourceDTO;
    }
}
