package com.example.batch.crawling;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
@AllArgsConstructor
public class TestController {

    private final CrawlingUtil crawlingUtil;

    @GetMapping("/test")
    public void crawl() throws IOException {
        crawlingUtil.getNews2();

    }



}
