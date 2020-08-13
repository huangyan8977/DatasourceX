package com.dtstack.dtcenter.common.loader.sqlserver;

import com.dtstack.dtcenter.common.exception.DBErrorCode;
import com.dtstack.dtcenter.common.exception.DtCenterDefException;
import com.dtstack.dtcenter.common.loader.common.AbsRdbmsClient;
import com.dtstack.dtcenter.common.loader.common.ConnFactory;
import com.dtstack.dtcenter.loader.DtClassConsistent;
import com.dtstack.dtcenter.loader.downloader.IDownloader;
import com.dtstack.dtcenter.loader.dto.ColumnMetaDTO;
import com.dtstack.dtcenter.loader.dto.SqlQueryDTO;
import com.dtstack.dtcenter.loader.dto.source.ISourceDTO;
import com.dtstack.dtcenter.loader.dto.source.RdbmsSourceDTO;
import com.dtstack.dtcenter.loader.dto.source.SqlserverSourceDTO;
import com.dtstack.dtcenter.loader.exception.DtLoaderException;
import com.dtstack.dtcenter.loader.source.DataSourceType;
import com.dtstack.dtcenter.loader.utils.DBUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @company: www.dtstack.com
 * @Author ：Nanqi
 * @Date ：Created in 15:30 2020/1/7
 * @Description：SqlServer 客户端
 */
public class SqlServerClient extends AbsRdbmsClient {
    private static final String TABLE_QUERY_ALL = "select a.name, b.name from sys.objects a left join sys.schemas b on a.schema_id = b.schema_id where a.type='U' or a.type='V'";
    private static final String TABLE_QUERY = "select a.name, b.name from sys.objects a left join sys.schemas b on a.schema_id = b.schema_id where a.type='U'";

    private static final String TABLE_SHOW = "[%s].[%s]";

    //获取所有的表和对应的schema-备用
    private static final String TABLE_QUERY_ALL_SCHEMA = "select sys.objects.name tableName,sys.schemas.name schemaName from sys.objects,sys.schemas where sys.objects.type='U' and sys.objects.schema_id=sys.schemas.schema_id";
    private static final String TABLE_QUERY_SCHEMA = "select sys.objects.name tableName,sys.schemas.name schemaName from sys.objects,sys.schemas where sys.objects.type='U' or type='V' and sys.objects.schema_id=sys.schemas.schema_id";

    private static String SQL_SERVER_COLUMN_NAME = "column_name";
    private static String SQL_SERVER_COLUMN_COMMENT = "column_description";
    private static final String COMMENT_QUERY = "SELECT B.name AS column_name, C.value AS column_description FROM sys.tables A INNER JOIN sys.columns B ON B.object_id = A.object_id LEFT JOIN sys.extended_properties C ON C.major_id = B.object_id AND C.minor_id = B.column_id WHERE A.name = N";

    @Override
    protected ConnFactory getConnFactory() {
        return new SQLServerConnFactory();
    }

    @Override
    protected DataSourceType getSourceType() {
        return DataSourceType.SQLServer;
    }

    @Override
    public List<String> getTableList(ISourceDTO iSource, SqlQueryDTO queryDTO) throws Exception {
        SqlserverSourceDTO sqlserverSourceDTO = (SqlserverSourceDTO) iSource;
        Integer clearStatus = beforeQuery(sqlserverSourceDTO, queryDTO, false);

        Statement statement = null;
        ResultSet rs = null;
        List<String> tableList = new ArrayList<>();
        try {
            String sql = queryDTO.getView() ? TABLE_QUERY_ALL : TABLE_QUERY;
            statement = sqlserverSourceDTO.getConnection().createStatement();
            rs = statement.executeQuery(sql);
            int columnSize = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                tableList.add(String.format(TABLE_SHOW, rs.getString(2), rs.getString(1)));
            }
        } catch (Exception e) {
            throw new DtCenterDefException("获取表异常", e);
        } finally {
            DBUtil.closeDBResources(rs, statement, sqlserverSourceDTO.clearAfterGetConnection(clearStatus));
        }
        return tableList;
    }

    @Override
    public String getTableMetaComment(ISourceDTO iSource, SqlQueryDTO queryDTO) throws Exception {
        SqlserverSourceDTO sqlserverSourceDTO = (SqlserverSourceDTO) iSource;
        Integer clearStatus = beforeColumnQuery(sqlserverSourceDTO, queryDTO);

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = sqlserverSourceDTO.getConnection().createStatement();
            resultSet = statement.executeQuery(
                    "select c.name, cast(isnull(f.[value], '') as nvarchar(100)) as REMARKS\n" +
                    "from sys.objects c " +
                    "left join sys.extended_properties f on f.major_id = c.object_id and f.minor_id = 0 and f.class = 1\n" +
                    "where c.type = 'u'");
            while (resultSet.next()) {
                String dbTableName = resultSet.getString(1);
                if (dbTableName.equalsIgnoreCase(queryDTO.getTableName())) {
                    return resultSet.getString(DtClassConsistent.PublicConsistent.REMARKS);
                }
            }
        } catch (Exception e) {
            throw new DtCenterDefException(String.format("获取表:%s 的信息时失败. 请联系 DBA 核查该库、表信息.",
                    queryDTO.getTableName()),
                    DBErrorCode.GET_COLUMN_INFO_FAILED, e);
        } finally {
            DBUtil.closeDBResources(resultSet, statement, sqlserverSourceDTO.clearAfterGetConnection(clearStatus));
        }
        return null;
    }

    @Override
    public IDownloader getDownloader(ISourceDTO source, SqlQueryDTO queryDTO) throws Exception {
        SqlserverSourceDTO sqlserverSourceDTO = (SqlserverSourceDTO) source;
        SqlServerDownloader sqlServerDownloader = new SqlServerDownloader(getCon(sqlserverSourceDTO), queryDTO.getSql(), sqlserverSourceDTO.getSchema());
        sqlServerDownloader.configure();
        return sqlServerDownloader;
    }

    @Override
    protected String dealSql(SqlQueryDTO sqlQueryDTO) {
        return "select top "+sqlQueryDTO.getPreviewNum()+" * from "+transferTableName(sqlQueryDTO.getTableName());
    }

    @Override
    protected String transferTableName(String tableName) {
        //如果传过来是[tableName]格式直接当成表名
        if (tableName.startsWith("[") && tableName.endsWith("]")){
            return tableName;
        }
        //如果不是上述格式，判断有没有"."符号，有的话，第一个"."之前的当成schema，后面的当成表名进行[tableName]处理
        if (tableName.contains(".")) {
            //切割，表名中可能会有包含"."的情况，所以限制切割后长度为2
            String[] tables = tableName.split("\\.", 2);
            tableName = tables[1];
            return String.format("%s.%s", tables[0], tableName.contains("[") ? tableName : String.format("[%s]",
                    tableName));
        }
        //判断表名
        return String.format("[%s]", tableName);
    }

    @Override
    public List<String> getAllDatabases(ISourceDTO source, SqlQueryDTO queryDTO) throws Exception {
        throw new DtLoaderException("Not Support");
    }

    @Override
    public String getCreateTableSql(ISourceDTO source, SqlQueryDTO queryDTO) throws Exception {
        throw new DtLoaderException("Not Support");
    }

    @Override
    public List<ColumnMetaDTO> getPartitionColumn(ISourceDTO source, SqlQueryDTO queryDTO) throws Exception {
        throw new DtLoaderException("Not Support");
    }

    @Override
    protected Map<String, String> getColumnComments(RdbmsSourceDTO sourceDTO, SqlQueryDTO queryDTO) throws Exception {
        Integer clearStatus = beforeColumnQuery(sourceDTO, queryDTO);
        Statement statement = null;
        ResultSet rs = null;
        Map<String, String> columnComments = new HashMap<>();
        try {
            statement = sourceDTO.getConnection().createStatement();
            String queryColumnCommentSql = COMMENT_QUERY + addSingleQuotes(queryDTO.getTableName());
            rs = statement.executeQuery(queryColumnCommentSql);
            while (rs.next()) {
                String columnName = rs.getString(SQL_SERVER_COLUMN_NAME);
                String columnComment = rs.getString(SQL_SERVER_COLUMN_COMMENT);
                columnComments.put(columnName, columnComment);
            }

        } catch (Exception e) {
            //获取表字段注释失败
        }finally {
            DBUtil.closeDBResources(rs, statement, sourceDTO.clearAfterGetConnection(clearStatus));
        }
        return columnComments;
    }

    private static String addSingleQuotes(String str) {
        str = str.contains("'") ? str : String.format("'%s'", str);
        return str;
    }
}
