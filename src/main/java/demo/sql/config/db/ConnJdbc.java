package demo.sql.config.db;

import com.zaxxer.hikari.HikariDataSource;
import demo.sql.util.DBPasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ConnJdbc implements InitializingBean {
    private static Logger LOGGER = LoggerFactory.getLogger(ConnJdbc.class);


    /**
     * SpringBoot获取当前环境代码,Spring获取当前环境代码
     */
    @Value("${spring.profiles.active}")
    private String profiles;

    @Autowired
    @Qualifier("JdbcTemplateStone")
    public JdbcTemplate jdbcTemplate;


    @Override
    public void afterPropertiesSet() throws Exception {
        HikariDataSource ds = (HikariDataSource) jdbcTemplate.getDataSource();
        if (ds != null) {
            ds.setIdleTimeout(30000);
            ds.setConnectionTimeout(30000);
            ds.setValidationTimeout(3000);
            ds.setLoginTimeout(5);
            ds.setMaxLifetime(60000);
            ds.setLeakDetectionThreshold(180000);
            String url = ds.getJdbcUrl();
            String user = ds.getUsername();
            if (profiles.contains("prod") && "wu_wdp".equals(user)) {
                // 说明连的是temp站或者stone,需要从密码服务中获取密码
                log.info("访问stonetemp,准备获取密码...");
                String password = DBPasswordUtil.GetLastedPassword("stonetemp", "wu_wdp");
                if (StringUtils.isNotEmpty(password)) {
                    log.info("stonetemp密码获取成功:{}", password);
                    ds.setPassword(password);
                } else {
                    log.warn("stonetemp数据库密码获取失败");


                }
            } else {
                log.info("访问stonetemp无需获得密码...");
            }
        }
    }
}
