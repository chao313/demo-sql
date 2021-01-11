package demo.sql.db.oracle;

import demo.sql.db.DefaultExport;
import demo.sql.util.CalciteOracleParseUtils;
import org.apache.calcite.sql.parser.SqlParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * oracle的导出
 */
@Component("OracleExport")
public class OracleExport extends DefaultExport {

    private static ThreadLocal<JdbcTemplate> threadPoolJdbcTemplate = new InheritableThreadLocal<>();

    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        threadPoolJdbcTemplate.set(jdbcTemplate);
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return threadPoolJdbcTemplate.get();
    }

    @Override
    public List<String> getFields(String sql) throws SqlParseException {
        return CalciteOracleParseUtils.getSimpleSelectList(sql);
    }

    @Override
    public String getTable(String sql) throws SqlParseException {
        return CalciteOracleParseUtils.getFrom(sql).toString();
    }
}
