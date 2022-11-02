package com.dtstack.dtcenter.loader.client;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.io.resource.InputStreamResource;
import com.dtstack.dtcenter.common.loader.gfdfs.GfdfsClientSpecial;
import com.dtstack.dtcenter.loader.dto.source.GfdfsSourceDTO;
import com.dtstack.dtcenter.loader.exception.DtLoaderException;
import com.dtstack.dtcenter.loader.source.DataSourceType;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author wlq
 * @create 2022-10-09 15:17
 */
public class GfdfsTest extends BaseTest {


    // 构建client
    private static final IClient client1 = ClientCache.getClient(DataSourceType.GFDFS.getVal());
    private static final IGfdfs client = ClientCache.getGfdfs(DataSourceType.GFDFS.getVal());


    private static final GfdfsSourceDTO source = GfdfsSourceDTO.builder()
            .ip("10.2.14.122") //10.2.18.16
            .port("8001") //8001
            .group("group1")
            .build();
    //测试连接
    @Test
    public void testConGfdfs() {
        Boolean isConnected = client1.testCon(source);
        if (Boolean.FALSE.equals(isConnected)) {
            throw new DtLoaderException("connection exception");
        }else {
            System.out.println("succeed");
        }
    }
    //测试上传文件
    @Test
    public void testUpload() {
        //文件地址
        File file = new File("C:\\Users\\19135\\Pictures\\Saved Pictures\\微信图片_20221013123437.jpg");
        String result = client.uploadFile(source, file);
        System.out.println(result);
    }
    //测试删除文件 参数：path
    @Test
    public void testDeleteByPath() {
        //文件路径
        String path = new String("/group1/image/20221014/15/04/0/微信图片_20221013123117.jpg");
        String s = client.deleteFile(source, path);
        System.out.println(s);
    }
    //测试删除文件 参数：md5
    @Test
    public void testDeleteByMd5() {
        //文件路径
        String md5 = new String("e60393a2b226ba7a2fe52454318aa048");
        String s = client.deleteFileByMd5(source, md5);
        System.out.println(s);
    }
    //测试获取文件信息 参数：path
    @Test
    public void testGetFileInfoByPath() {
        //文件路径
        String path = new String("/group1/image/20221014/16/07/0/微信图片_20221013123117.jpg");
        String s = client.getFileInfoByPath(source, path);
        System.out.println(s);
    }
    //测试获取文件信息 参数：md5
    @Test
    public void testGetFileInfoByMd5() {
        //文件路径
        String md5 = new String("e60393a2b226ba7a2fe52454318aa048");
        String s = client.getFileInfoByMd5(source, md5);
        System.out.println(s);
    }
    //获取文件列表
    @Test
    public void testGetFileListDir() {
        //要获取的目录名称
        String dir = new String("image");
        String s = client.getFileListDir(source, dir);
        System.out.println(s);
    }
    //文件统计
    @Test
    public void testFileStat() {
        String s = client.getFileStat(source);
        System.out.println(s);
    }
    //流式上传文件
    @Test
    public void testUploadInputStreamToGfdfs() {
        File file = FileUtil.file("C:\\Users\\19135\\Pictures\\Saved Pictures\\微信图片_20221013123437.jpg");
        String name = FileNameUtil.getName(file);
        FileInputStream fileInputStream = IoUtil.toStream(file);
        byte[] bytes = IoUtil.readBytes(fileInputStream);
        System.out.println(name);
        String sou = client.uploadInputStreamToGfdfs(source, bytes, name);
        System.out.println(sou);
    }


}
