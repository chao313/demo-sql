package demo.sql.db;

import demo.sql.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.parser.SqlParseException;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 默认的导出功能
 */
@Slf4j
public abstract class DefaultExport implements Export {


    @Override
    public void export(String sql, Consumer<ResultSet> consumer) {
        this.getJdbcTemplate().setFetchSize(200);//流式读取
        this.getJdbcTemplate().query(sql, rs -> {
            do {
                consumer.accept(rs);
            } while (rs.next());
        });
    }

    /**
     * 导出为List格式
     *
     * @param sql
     * @return
     */
    public List<List<String>> exportToList(String sql) throws SqlParseException {
        List<List<String>> lists = new ArrayList<>();
        List<String> fields = this.getFields(sql);
        //添加头
        lists.add(fields);
        this.export(sql, resultSet -> {
            //一条记录
            List<String> record = new ArrayList<>();
            for (String field : fields) {
                try {
                    record.add(resultSet.getString(field));
                } catch (SQLException e) {
                    log.error("获取字段异常:{} -> 记录:{}", field, record);
                }
            }
            lists.add(record);
        });
        return lists;
    }

    /**
     * 导出为List格式
     *
     * @param sql
     * @return
     */
    public void exportToCSV(String sql, OutputStream outputStream) throws SqlParseException, IOException {
        long start = System.currentTimeMillis();
        ArrayBlockingQueue<List<String>> queue = new ArrayBlockingQueue<>(100000);
        List<String> fields = this.getFields(sql);
        //使用队列
        AtomicBoolean isStop = new AtomicBoolean(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                export(sql, resultSet -> {
                    //一条记录
                    List<String> record = new ArrayList<>();
                    for (String field : fields) {
                        try {
                            record.add(resultSet.getString(field));
                        } catch (SQLException e) {
                            log.error("获取字段异常:{} -> 记录:{}", field, record);
                        }
                    }
                    try {
                        queue.put(record);
                    } catch (InterruptedException e) {
                        log.info("queue full -> 等待:{}", e.toString(), e);
                    }
                });
                isStop.set(true);//代表结束
            }
        }).start();

        AtomicInteger write = new AtomicInteger();
        while (isStop.get() == false || queue.size() > 0) {
            //没有结束 或者 队列的size >0 就一只循环
            List<String> poll = queue.poll();
            if (null != poll) {
                ExcelUtil.writeListCSV(Arrays.asList(poll), outputStream, (line, size) -> {
                    write.getAndIncrement();
                    if (write.get() % PROCESS_LIMIT == 0) {
                        long use = System.currentTimeMillis() - start;//耗时 毫秒
                        BigDecimal numPeerSecond = new BigDecimal(write.get()).divide(new BigDecimal(use), 3, RoundingMode.HALF_DOWN).multiply(new BigDecimal(1000));//每秒处理的数量
                        log.info("写入进度:{} ,以使用耗时(Second):{} ,每秒处理的数量:{}", write.get(), use / 1000, numPeerSecond);
                    }
                });
            }
        }
        log.info("执行结束:{}", write.get());

    }


}
