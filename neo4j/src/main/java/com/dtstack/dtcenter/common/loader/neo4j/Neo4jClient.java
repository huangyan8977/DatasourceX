package com.dtstack.dtcenter.common.loader.neo4j;

import com.dtstack.dtcenter.common.loader.common.nosql.AbsNoSqlClient;
import com.dtstack.dtcenter.common.loader.neo4j.utils.Neo4jUtils;
import com.dtstack.dtcenter.loader.dto.source.ISourceDTO;

/**
 * @author 10819
 * @description: neo4j client
 * @date 10/24/2022 2:59 PM
 */
public class Neo4jClient<T> extends AbsNoSqlClient<T> {
    /**
     * 测试连通
     * @param source
     * @return
     */
    @Override
    public Boolean testCon(ISourceDTO source) {
        return Neo4jUtils.checkConnection(source);
    }

}
