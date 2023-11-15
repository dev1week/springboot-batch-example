package com.example.batch.crawling.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrawlingDto {

    private String imgUrl;
    private String url;
    private String title;
    private String content;
    private String press;


}
