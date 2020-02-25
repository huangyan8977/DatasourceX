package com.dtstack.dtcenter.common.loader.nosql.redis;

import com.dtstack.dtcenter.common.loader.nosql.common.AbsNosqlClient;
import com.dtstack.dtcenter.loader.dto.SourceDTO;

import java.util.List;
import java.util.Map;

/**
 * @company: www.dtstack.com
 * @Author ：Nanqi
 * @Date ：Created in 15:38 2020/2/4
 * @Description：Redis 客户端
 */
public class RedisClient extends AbsNosqlClient {
    @Override
    public Boolean testCon(SourceDTO source) throws Exception {
        return RedisUtils.checkConnection(source);
    }

    @Override
    public List<Map<String, Object>> executeQuery(SourceDTO source, String sql) throws Exception {
        return null;
    }
}