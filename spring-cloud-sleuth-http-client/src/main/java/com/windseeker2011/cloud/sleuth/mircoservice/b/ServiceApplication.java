package com.windseeker2011.cloud.sleuth.mircoservice.b;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 第二个微服务
 *
 * @author Weihai Li
 */
@RestController
@Slf4j
@EnableEurekaClient
@SpringBootApplication
public class ServiceApplication {

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private Tracer tracer;

    @Bean
//	@LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 一层调用
     *
     * @return
     */
    @GetMapping(value = "/1")
    public String m1() {
        log.info("我是微服务2号。。。");
        return "success";
    }

    /**
     * 二层调用
     *
     * @return
     */
    @GetMapping(value = "/2")
    public String m2() {
        log.info("我是微服务2号。。。");
        List<Future<ResponseEntity<String>>> respList = IntStream.range(0, 3).mapToObj((i) -> {
                    return threadPoolExecutor.submit(() -> {
                        ResponseEntity<String> result = restTemplate().getForEntity("http://127.0.0.1:8883/1", String.class);
                        return result;
                    });
                })
                .collect(Collectors.toList());
        // Start a span. If there was a span present in this thread it will become
// the `newSpan`'s parent.
        Span newSpan = this.tracer.nextSpan().name("calculateTax");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())) {
            // ...
            // You can tag a span
            log.info("test taxValue");
            newSpan.tag("taxValue", "oooo");
            // ...
            // You can log an event on a span
            newSpan.event("taxCalculated");
        } finally {
            // Once done remember to end the span. This will allow collecting
            // the span to send it to a distributed tracing system e.g. Zipkin
            newSpan.end();
        }
        return respList.toString();
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public static void main(String[] args) throws Exception {
        String[] args2 = new String[]{"--spring.profiles.active=b"};
        SpringApplication.run(ServiceApplication.class, args2);

    }

}
