package com.windseeker2011.cloud.sleuth.mircoservice.b;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BusinessService {
    @Autowired
    private BusinessService businessService;

    @NewSpan("complicatedLogicLevel1")
    public void complicatedLogicLevel1(@SpanTag("input") Integer input) {
        log.info("complicatedLogicLevel1");
        for (int i = 0; i < input; i++) {
            businessService.complicatedLogicLevel2(input);
        }
    }

    @NewSpan("complicatedLogicLevel2")
    public void complicatedLogicLevel2(@SpanTag("input") Integer input) {
        for (int i = 0; i < input; i++) {
            businessService.complicatedLogicLevel3(input);
        }
        log.info("complicatedLogicLevel2");
    }

    @NewSpan("complicatedLogicLevel3")
    public void complicatedLogicLevel3(@SpanTag("input") Integer input) {
        log.info("complicatedLogicLevel3");
    }
}
