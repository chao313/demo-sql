package demo.sql.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.util.NlsString;

import java.util.*;

/**
 * 解析语句的工具类
 */
@Slf4j
public class CalciteOracleParseUtils {

    private static final SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.ORACLE).setCaseSensitive(true).build();//使用mysql 语法

    private static final List<SqlKind> sqlKinds = Arrays.asList(SqlKind.AND, SqlKind.OR);

    /**
     * 获取语句类型 select/delete/update
     */
    public static SqlKind getKind(String sql) throws SqlParseException {
        //SqlParser 语法解析器
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        SqlKind sqlKind = sqlNode.getKind();
        return sqlKind;
    }

    /**
     * 获取检索的字段
     */
    public static SqlNodeList getSelectList(String sql) throws SqlParseException {
        //SqlParser 语法解析器
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        SqlSelect sqlSelect = getSqlSelect(sqlNode);
        SqlNodeList sqlNodeList = sqlSelect.getSelectList();
        return sqlNodeList;
    }


    /**
     * 获取检索的字段
     */
    public static List<String> getSimpleSelectList(String sql) throws SqlParseException {
        SqlNodeList sqlNodeList = getSelectList(sql);
        List<String> result = new ArrayList<>();
        sqlNodeList.getList().forEach(sqlNodeTmp -> {
            result.add(sqlNodeTmp.toString());
        });
        return result;
    }

    /**
     * 获取检索的表
     * 注意！ 关联检索存在问题!
     */
    public static SqlNode getFrom(String sql) throws SqlParseException {
        //SqlParser 语法解析器
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        SqlSelect sqlSelect = getSqlSelect(sqlNode);
        SqlNode from = sqlSelect.getFrom();
        return from;
    }

    /**
     * 获取检索的where
     */
    public static SqlBasicCall getWhere(String sql) throws SqlParseException {
        //SqlParser 语法解析器
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        SqlSelect sqlSelect = getSqlSelect(sqlNode);
        SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlSelect.getWhere();
        return sqlBasicCall;
    }

    /**
     * 获取检索的where 这里是简单的只有and的解析
     */
    public static List<SqlBasicCall> getWhereSimpleSqlBasicCall(String sql) throws SqlParseException {
        SqlBasicCall sqlBasicCall = CalciteOracleParseUtils.getWhere(sql);
        List<SqlBasicCall> sqlBasicCalls = new ArrayList<>();
        if (null != sqlBasicCall) {
            //如果没有where -> 这里可能为空
            if (sqlKinds.contains(sqlBasicCall.getOperator().getKind())) {
                //如果是AND 和 OR -> 遍历
                getSimpleSqlBasicCalls(sqlBasicCall, sqlBasicCalls);
            } else {
                //如果是其他
                sqlBasicCalls.add(sqlBasicCall);
            }
        }
        return sqlBasicCalls;
    }

    /**
     * 获取全部的
     *
     * @param sqlBasicCall
     * @param sqlBasicCalls
     */
    private static void getSimpleSqlBasicCalls(SqlBasicCall sqlBasicCall, List<SqlBasicCall> sqlBasicCalls) {
        Arrays.stream(sqlBasicCall.getOperands()).forEach(sqlNode -> {
            if (sqlNode instanceof SqlBasicCall) {
                SqlBasicCall tmp = ((SqlBasicCall) sqlNode);
                if (sqlKinds.contains(tmp.getOperator().getKind())) {
                    getSimpleSqlBasicCalls(((SqlBasicCall) sqlNode), sqlBasicCalls);
                } else {
                    sqlBasicCalls.add(tmp);
                }
            } else {
                log.error("错误:{}");
            }
        });
    }


    /**
     * 解析简单的
     *
     * @param sql
     */
    public static void getKine(String sql) {

        //SqlParser 语法解析器
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = null;
        try {
            sqlNode = sqlParser.parseStmt();
            SqlSelect sqlSelect = ((SqlSelect) sqlNode);
            SqlKind sqlKind = sqlSelect.getKind();//获取语句类型 select/delete/update
            log.info("解析:kind:{}", sqlKind);
            SqlNodeList sqlNodeList = sqlSelect.getSelectList();
            log.info("解析:sqlNodeList:{}", sqlNodeList);
            SqlNode from = sqlSelect.getFrom();
            log.info("解析:from:{}", from);
            SqlNode where = sqlSelect.getWhere();
            log.info("解析:where:{}", where);
            if (where instanceof SqlBasicCall) {
                SqlBasicCall sqlBasicCall = (SqlBasicCall) where;
                log.info("sqlBasicCall:{}", sqlBasicCall);
                SqlKind kind = sqlBasicCall.getKind();
                log.info("顶级:{}", kind);
                SqlNode[] operands = sqlBasicCall.getOperands();
                for (int i = 0; i < operands.length; i++) {
                    SqlNode operand = operands[i];
                    log.info("operand:{}", operand);
                }

            }
        } catch (SqlParseException e) {
            throw new RuntimeException("", e);

        }

    }

    public static String getValue(SqlNode sqlNode) {
        if (sqlNode instanceof SqlIdentifier) {
            return ((SqlIdentifier) sqlNode).getSimple();
        }
        if (sqlNode instanceof SqlCharStringLiteral) {
            Object value = ((SqlCharStringLiteral) sqlNode).getValue();
            if (value instanceof NlsString) {
                return ((NlsString) value).getValue();
            } else {
                return value.toString();
            }
        }
        return sqlNode.toString();
    }

    /**
     * 获取order list
     *
     * @param sql
     * @return
     */
    public static Map<String, String> getSqlOrderMap(String sql) throws SqlParseException {
        SqlNodeList sqlNodes = getSqlOrder(sql);
        Map<String, String> result = new LinkedHashMap<>();
        if (null != sqlNodes) {
            sqlNodes.getList().forEach(sqlNode -> {
                if (sqlNode instanceof SqlBasicCall) {
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlNode;
                    SqlKind kind = sqlBasicCall.getKind();
                    if (kind.equals(SqlKind.DESCENDING)) {
                        //倒序
                        SqlNode[] operands = sqlBasicCall.getOperands();
                        Arrays.stream(operands).forEach(operand -> {
                            result.put(operand.toString(), "desc");
                        });
                    } else {
                        throw new RuntimeException("预料之外的语法:" + kind);
                    }

                } else if (sqlNode instanceof SqlIdentifier) {
                    SqlIdentifier sqlIdentifier = (SqlIdentifier) sqlNode;
                    result.put(sqlIdentifier.toString(), "asc");
                } else {
                    throw new RuntimeException("预料之外的语法:" + sqlNode);
                }
            });
        }
        return result;
    }

    /**
     * 获取order list
     *
     * @param sql
     * @return
     */
    public static SqlNodeList getSqlOrder(String sql) throws SqlParseException {
        //SqlParser 语法解析器
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlNode sqlNode = sqlParser.parseStmt();
        SqlNodeList order = null;
        if (sqlNode instanceof SqlOrderBy) {
            //如果是order 语法
            SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
            order = sqlOrderBy.orderList;
        }
        return order;
    }

    /**
     * 兼容 select 和 order语法
     *
     * @param sqlNode
     * @return
     */
    private static SqlSelect getSqlSelect(SqlNode sqlNode) {
        SqlSelect sqlSelect = null;
        if (sqlNode instanceof SqlOrderBy) {
            //如果是order 语法
            SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
            SqlNode query = sqlOrderBy.query;
            if (query instanceof SqlSelect) {
                sqlSelect = (SqlSelect) query;
            } else {
                throw new RuntimeException("解析sql异常:" + query);
            }
        } else if (sqlNode instanceof SqlSelect) {
            //普通的 select
            sqlSelect = (SqlSelect) sqlNode;
        }
        return sqlSelect;
    }
}


