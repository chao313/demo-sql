package demo.sql.db;

import org.apache.calcite.sql.parser.SqlParseException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * 导出的上层接口
 */
public interface Export {

    static final Integer PROCESS_LIMIT = 10000;//进度处理


    /**
     * 不同的实现存在不同的JdbcTemplate
     *
     * @return
     */

    void setJdbcTemplate(JdbcTemplate jdbcTemplate);

    /**
     * 不同的实现存在不同的JdbcTemplate
     *
     * @return
     */
    JdbcTemplate getJdbcTemplate();


    /**
     * 不同的实现存在不同的字段解析方法
     *
     * @param sql
     * @return
     */
    List<String> getFields(String sql) throws SqlParseException;

    /**
     * 不同的实现存在不同的字段解析方法
     *
     * @param sql
     * @return
     */
    String getTable(String sql) throws SqlParseException;

    /**
     * 根据sql语句导出为
     *
     * @param sql
     * @param consumer
     */
    void export(String sql, Consumer<ResultSet> consumer);


}
