package demo.sql.framework.aspect;//package demo.kafka.framework.aspect;
//
//import demo.kafka.framework.Code;
//import demo.kafka.framework.Response;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.text.MessageFormat;
//
///**
// * AuthHeader 从请求头中验证
// */
//@org.aspectj.lang.annotation.Aspect
//@Component
//public class AuthHeaderAspect {
//    protected final Logger logger = LoggerFactory.getLogger(getClass());
//
//    /**
//     * 使用execution表达式
//     * 拦截controller目录下的auth包中进行拦截
//     */
//    @Pointcut("execution(* demo.spring.boot.demospringboot.controller.auth.*.*(..))")
//    public void pointCut() {
//    }
//
//
//    /**
//     * 拦截验证Header
//     */
//    @Around(value = "pointCut()")
//    public Object beforeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        ServletRequestAttributes servletRequestAttributes
//                = (ServletRequestAttributes) requestAttributes;
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        String authorization = request.getHeader("Authorization");
//        logger.info("【http请求Header拦截】:{}", authorization);
//        if (null == authorization || "".equals(authorization) || !authorization.startsWith("Bearer ")) {
//            String message = MessageFormat.format("【http请求Header拦截】Header中Authorization格式不正确 {0}", authorization);
//            logger.warn(message);
//            Response response = new Response();
//            response.setCode(Code.System.FAIL);
//            response.setMsg(message);
//            return response;
//        } else {
//            logger.warn("【http请求Header拦截】:authorization请求头正常:{}", authorization);
//            return joinPoint.proceed();
//        }
//
//    }
//}
