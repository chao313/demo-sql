package demo.sql.framework.aspect;//package demo.kafka.framework.aspect;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//
///**
// * 2018/8/9    Created by   chao
// */
//@org.aspectj.lang.annotation.Aspect
//@Component
//public class AnnotationsAspect {
//    protected final Logger logger = LoggerFactory.getLogger(getClass());
//
//    /**
//     * 使用注解定义切面执行的方法
//     */
//    @Pointcut(value = "@annotation(demo.spring.boot.demospringboot.framework.annotations.CustomAnnotation)")
//    public void pointCut() {
//    }
//
//
//    /**
//     * Before
//     */
//    @Before(value = "pointCut()")
//    public void beforeAdvice(JoinPoint joinPoint) {
//        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//        Method method = methodSignature.getMethod();
//        //如果方法体上使用了CustomAnnotation注解
//        if (method.isAnnotationPresent(CustomAnnotation.class)) {
//            //获取该方法上的注解名
//            CustomAnnotation annotation = method.getAnnotation(CustomAnnotation.class);
//            logger.info("【CustomAnnotation的value】：{} ", annotation.value());
//            logger.info("【CustomAnnotation的required】：{} ", annotation.required());
//            //根据注解的值，进行权限拦截...操作
//        }
//
//
//    }
//
//
//}
