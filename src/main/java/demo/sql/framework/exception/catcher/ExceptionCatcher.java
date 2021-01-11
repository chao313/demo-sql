package demo.sql.framework.exception.catcher;


import demo.sql.framework.Code;
import demo.sql.framework.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局捕获异常
 */
@ControllerAdvice
@Slf4j
public class ExceptionCatcher {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Response Exception(Exception e) {
        /**
         * 用于捕获全局异常，Controller发生异常，如果没有处理，就会在这里统一处理
         */
        Response response = new Response();
        response.setCode(Code.System.SYSTEM_ERROR_CODE);
        response.setMsg(Code.System.SYSTEM_ERROR_CODE_MSG);
        response.addException(e);
        response.setError(e.getMessage());
        log.error("e:", e.toString(), e);
        return response;
    }
}
