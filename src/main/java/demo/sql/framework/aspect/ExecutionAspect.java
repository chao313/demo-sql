//package demo.sql.framework.aspect;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import demo.elastic.search.framework.Code;
//import demo.elastic.search.framework.Response;
//import demo.elastic.search.service.RedisService;
//import demo.elastic.search.thread.ThreadLocalFeign;
//import org.apache.commons.lang3.StringUtils;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
///**
// * 2018/8/9    Created by   chao
// */
//@Aspect
//@Component
//public class ExecutionAspect {
//    protected final Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private RedisService redisService;
//
//    /**
//     * 定义切面执行的方法
//     */
//    @Pointcut("execution(public * demo.elastic.search.controller..*..*(..))")
//    private void pointCut() {
//    }
//
//    /**
//     * ProceedingJoinPoint 继承的 JoinPoint 比JoinPoint ， 只多了执行的proceed Around </>一旦执行了这个方法，如果不调用proceed
//     * ， 就会导致调用终止 注意：当核心业务抛异常后，立即退出，转向AfterAdvice 执行完AfterAdvice，再转到ThrowingAdvice
//     */
//    @Around(value = "pointCut()")
//    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
////        logger.info("第一步【执行Around：拦截方法的签名】joinPoint.getSignature() - > {}", joinPoint.getSignature());//Response demo.spring.boot.demospringboot.controller.pub.FrameworkController.framework(String,String)
////        logger.info("第一步【执行Around：拦截目标的对象】joinPoint.getTarget() - > {}", joinPoint.getTarget());//demo.spring.boot.demospringboot.controller.pub.FrameworkController@25c6ab3f
////        logger.info("第一步【执行Around：获取链接点的静态部分】joinPoint.getStaticPart() - > {}", joinPoint.getStaticPart());//execution(Response demo.spring.boot.demospringboot.controller.pub.FrameworkController.framework(String,String))
////        logger.info("第一步【执行Around：拦截参数】joinPoint.getArgs() - > {}{}", joinPoint.getArgs());//王海潮123 | 注意这里要两个{}
////        logger.info("第一步【执行Around：拦截切面的类型】joinPoint.getKind() - > {}", joinPoint.getKind());//method-execution || exception-handler
////        logger.info("第一步【执行Around：拦截连接点方法所在类文件中的位置】joinPoint.getSourceLocation() - > {}", joinPoint.getSourceLocation());//org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint$SourceLocationImpl@6742526e
////        logger.info("第一步【执行Around：拦截AOP的当前执行对象】joinPoint.getThis() - > {}", joinPoint.getThis());//demo.spring.boot.demospringboot.controller.pub.FrameworkController@25c6ab3
//        ThreadLocalFeign.init();
//        Response response = new Response<>();
//        try {
//            Object result = joinPoint.proceed(); //继续下一个方法的调用 ：就是调用拦截的函数，得到拦截函数执行的结果
//            if (null != ThreadLocalFeign.getES_PAGE() && true == ThreadLocalFeign.getES_PAGE()) {
//                //走分页
//                if (result instanceof Response) {
//                    Object content = ((Response) result).getContent();
//                    if (content instanceof ArrayNode) {
//                        JSONArray jsonArray = JSONArray.parseArray(content.toString());
//                        List<Object> objects = Arrays.asList(jsonArray.toArray());
//                        //添加过滤
//                        if (StringUtils.isNotBlank(ThreadLocalFeign.geES_FILTER())) {
//                            objects = filter(objects);
//                        }
//                        String uuid = UUID.randomUUID().toString();
//                        redisTemplate.opsForList().leftPushAll(uuid, objects);//存入list
//                        Object resultPage = redisService.getRecordByScrollId(uuid, 1, ThreadLocalFeign.getES_PAGE_SIZE());
//                        ((Response) result).setContent(resultPage);
//                    }
//                }
//            }
//            if (result instanceof Response) {
//                Response tmp = ((Response) result);
//                if (tmp.getContent() instanceof JSONObject) {
//                    JSONObject tmpJsonObject = (JSONObject) tmp.getContent();
//                    if (tmpJsonObject.containsKey("error")) {
//                        //包含异常错误
//                        ((Response) result).setCode(Code.System.FAIL);
//                        ((Response) result).setMsg(tmpJsonObject.toJSONString());
//                    }
//                }
//            }
//            logger.info("执行结果:{}", JSONObject.toJSON(result));
//            return result;
//        } catch (Exception e) {
//            response.setCode(Code.System.FAIL);
//            response.setMsg(e.toString());
//            response.addException(e);
//            logger.error("[]FAIL path:{}", e.getMessage(), e);
//            return response;
//        }
//    }
//
//    private List<Object> filter(List<Object> sources) {
//        String s = ThreadLocalFeign.geES_FILTER();
//        JSONObject jsonObject = JSONObject.parseObject(s);
//        Map<String, Object> innerMap = jsonObject.getInnerMap();//过滤的map
//        List<Object> collect = sources.stream().filter(o -> {
//            if (o instanceof JSONObject) {
//                JSONObject tmp = (JSONObject) o;
//                boolean[] flags = new boolean[innerMap.size()];//记录每个条件的数组
//                AtomicInteger i = new AtomicInteger();
//                innerMap.forEach((key, val) -> {
//                    if (tmp.containsKey(key)) {
//                        Object value = tmp.get(key);
//                        String contain = val.toString();
//                        String regexToWildcard = val.toString().replace("*", ".*").replace("?", ".{1}");
//                        if (null == value) {
//                            //如果value 为null
//                            if (contain.equalsIgnoreCase("*") || contain.equalsIgnoreCase(".*")) {
//                                //如果为通配 -> flag标记一个为 true
//                                flags[i.getAndIncrement()] = true;
//                            }
//                        } else if (value.toString().contains(contain) || value.toString().matches(regexToWildcard)) {
//                            //如果相等一个 -> flag标记一个为 true
//                            flags[i.getAndIncrement()] = true;
//                        }
//                    }
//                });
//                boolean result = true;
//                for (boolean flag : flags) {
//                    if (flag == false) {
//                        result = false;
//                        break;
//                    }
//                }
//                return result;
//            }
//            return true;
//        }).collect(Collectors.toList());
//        return collect;
//    }
//
//
////    /**
////     * Before
////     */
////    @Before(value = "pointCut()")
////    public void beforeAdvice(JoinPoint joinPoint) {
////        logger.info("第二步【执行Before：拦截方法的签名】joinPoint.getSignature() - > {}", joinPoint.getSignature());
////        logger.info("第二步【执行Before：拦截目标的对象】joinPoint.getTarget() - > {}", joinPoint.getTarget());
////        logger.info("第二步【执行Before：获取链接点的静态部分】joinPoint.getStaticPart() - > {}", joinPoint.getStaticPart());
////        logger.info("第二步【执行Before：拦截参数】joinPoint.getArgs() - > {}{}", joinPoint.getArgs());
////        logger.info("第二步【执行Before：拦截切面的类型】joinPoint.getKind() - > {}", joinPoint.getKind());
////        logger.info("第二步【执行Before：拦截连接点方法所在类文件中的位置】joinPoint.getSourceLocation() - > {}", joinPoint.getSourceLocation());
////        logger.info("第二步【执行Before：拦截AOP的当前执行对象】joinPoint.getThis() - > {}", joinPoint.getThis());
////    }
////
////
////    /**
////     * After 核心业务逻辑退出后（包括正常执行结束和异常退出），执行此Advice
////     */
////    @After(value = "pointCut()")
////    public void afterAdvice(JoinPoint joinPoint) {
////        logger.info("第三步【执行After：拦截方法的签名】joinPoint.getSignature() - > {}", joinPoint.getSignature());
////        logger.info("第三步【执行After：拦截目标的对象】joinPoint.getTarget() - > {}", joinPoint.getTarget());
////        logger.info("第三步【执行After：获取链接点的静态部分】joinPoint.getStaticPart() - > {}", joinPoint.getStaticPart());
////        logger.info("第三步【执行After：拦截参数】joinPoint.getArgs() - > {}{}", joinPoint.getArgs());
////        logger.info("第三步【执行After：拦截切面的类型】joinPoint.getKind() - > {}", joinPoint.getKind());
////        logger.info("第三步【执行After：拦截连接点方法所在类文件中的位置】joinPoint.getSourceLocation() - > {}", joinPoint.getSourceLocation());
////        logger.info("第三步【执行After：拦截AOP的当前执行对象】joinPoint.getThis() - > {}", joinPoint.getThis());
////    }
////
////    /**
////     * 正常return执行
////     * <p>
////     * 在执行代码return之后执行，没有发生异常，执行
////     */
////    @AfterReturning(value = "pointCut()", returning = "retVal")
////    public void afterReturningAdvice(JoinPoint joinPoint, Object retVal) {
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截方法的签名】joinPoint.getSignature() - > {}", joinPoint.getSignature());
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截目标的对象】joinPoint.getTarget() - > {}", joinPoint.getTarget());
////        logger.info("第三步后-正常执行【正常return后AfterReturning：获取链接点的静态部分】joinPoint.getStaticPart() - > {}", joinPoint.getStaticPart());
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截参数】joinPoint.getArgs() - > {}{}", joinPoint.getArgs());
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截切面的类型】joinPoint.getKind() - > {}", joinPoint.getKind());
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截连接点方法所在类文件中的位置】joinPoint.getSourceLocation() - > {}", joinPoint.getSourceLocation());
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截AOP的当前执行对象】joinPoint.getThis() - > {}", joinPoint.getThis());
////
////
////        logger.info("第三步后-正常执行【正常return后AfterReturning：拦截return的值】joinPoint.getThis() - > {}", retVal);
////
//////        logger.debug("=======>方法正常退出记录日志切面结束");
////    }
////
////    /**
////     * 异常后执行，处理错误信息
////     */
////    @AfterThrowing(value = "pointCut()", throwing = "ex")
////    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
////        logger.info("第三步后-异常执行【异常return后AfterThrowing：拦截方法的签名】joinPoint.getSignature() - > {}", joinPoint.getSignature());
////        logger.info("第三步后-异常执行【异常return后AfterThrowing：拦截目标的对象】joinPoint.getTarget() - > {}", joinPoint.getTarget());
////        logger.info("第三步后-异常执行【异常return后AfterThrowing：获取链接点的静态部分】joinPoint.getStaticPart() - > {}", joinPoint.getStaticPart());
////        logger.info("第三步后-异常执行【异常return后AfterThrowing：拦截参数】joinPoint.getArgs() - > {}{}", joinPoint.getArgs());
////        logger.info("第三步后-异常执行【异常return后AfterThrowing：拦截切面的类型】joinPoint.getKind() - > {}", joinPoint.getKind());
////        logger.info("第三步后-异常执行【异常return后AfterThrowing：拦截连接点方法所在类文件中的位置】joinPoint.getSourceLocation() - > {}", joinPoint.getSourceLocation());
////
////    }
//
//}
