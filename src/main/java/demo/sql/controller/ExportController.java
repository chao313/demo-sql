package demo.sql.controller;


import demo.sql.controller.resource.service.ResourceService;
import demo.sql.db.oracle.OracleExport;
import demo.sql.framework.Code;
import demo.sql.framework.Response;
import demo.sql.util.DateUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 导出
 */
@Slf4j
@RestController
@RequestMapping(value = "/ExportController")
public class ExportController {

    @Autowired
    @Qualifier("JdbcTemplateStone")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("OracleExport")
    private OracleExport oracleExport;

    @Autowired
    private ResourceService resourceService;

    @ApiOperation(value = "导出为 List<List<String>>")
    @GetMapping("/getOracleExportToList")
    public Response getOracleExportToList(@RequestParam(value = "sql", required = false) String sql) {
        Response response = new Response<>();
        try {
            oracleExport.setJdbcTemplate(jdbcTemplate);
            List<List<String>> lists = oracleExport.exportToList(sql);
            response.setCode(Code.System.OK);
            response.setContent(lists.size());
        } catch (Exception e) {
            response.setCode(Code.System.FAIL);
            response.setMsg(e.getMessage());
            response.addException(e);
            log.error("异常 ：{} ", e.getMessage(), e);
        }
        return response;

    }

    @ApiOperation(value = "导出为 CSV")
    @GetMapping("/getOracleExportToCSV")
    public Response getOracleExportToCSV(@RequestParam(value = "sql", required = false) String sql,
                                         @ApiParam(hidden = true) @RequestHeader(value = "host") String host,
                                         HttpServletRequest httpServletRequest) {
        Response response = new Response<>();
        try {
//            String table = oracleExport.getTable(sql);
            String table = "";
            String fileName = table + DateUtil.getNow() + ".csv";
            File file = resourceService.addNewFile(fileName);
            oracleExport.setJdbcTemplate(jdbcTemplate);
            oracleExport.exportToCSV(sql, new FileOutputStream(file));
            response.setCode(Code.System.OK);
            response.setContent(resourceService.getDownloadByFileName(host, fileName));
        } catch (Exception e) {
            response.setCode(Code.System.FAIL);
            response.setMsg(e.getMessage());
            response.addException(e);
            log.error("异常 ：{} ", e.getMessage(), e);
        }
        return response;

    }

}
