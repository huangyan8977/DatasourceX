package com.dtstack.dtcenter.common.loader.rdbms.mysql;

import com.dtstack.dtcenter.common.exception.DtCenterDefException;
import com.dtstack.dtcenter.common.loader.rdbms.common.AbsRdbmsClient;
import com.dtstack.dtcenter.loader.dto.SourceDTO;
import org.junit.Test;

public class MysqlClientTest {
    private static AbsRdbmsClient rdbsClient = new MysqlClient();

    @Test
    public void getConnFactory() throws Exception {
        SourceDTO source = new SourceDTO.SourceDTOBuilder()
                .setUrl("jdbc:mysql://172.16.8.109:3306/ide")
                .setUsername("dtstack")
                .setPassword("abc123")
                .builder();
        Boolean isConnected = rdbsClient.testCon(source);
        if (!isConnected) {
            throw new DtCenterDefException("数据源连接异常");
        }
    }
}