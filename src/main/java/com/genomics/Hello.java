package com.genomics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jiangjinman on 2017/4/21.
 */
@RestController
public class Hello {
    private final Logger log = LoggerFactory.getLogger(Hello.class);
    @RequestMapping("/")
    @ResponseBody
   public String sayHello() {
        String text="lemon";
        log.info("hello,{}",text);
        return "Hello lemon!";//返回结果为字符串
    }
}
