package demo.sql.config.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {


    @Bean(name = "DataSourceStone")
    @Qualifier("DataSourceStone")
    @ConfigurationProperties(prefix = "datasource.stone")
    public DataSource stoneDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "JdbcTemplateStone")
    @Qualifier("JdbcTemplateStone")
    public JdbcTemplate stonetempJdbcTemplate(
            @Qualifier("DataSourceStone") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


//    @Bean(name = "DataSourceSqllite")
//    @Qualifier("DataSourceSqllite")
//    @ConfigurationProperties(prefix = "datasource.sqllite")
//    public DataSource sqlliteDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "JdbcTemplateSqllite")
//    @Qualifier("JdbcTemplateSqllite")
//    public JdbcTemplate sqllitetempJdbcTemplate(
//            @Qualifier("DataSourceSqllite") DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }

}
