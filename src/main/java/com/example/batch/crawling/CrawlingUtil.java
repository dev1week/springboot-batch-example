package com.example.batch.crawling;


import com.example.batch.crawling.dto.CrawlingDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class CrawlingUtil {

    private static String url = "https://news.naver.com/main/ranking/popularDay.naver";

    public static void getNews() throws IOException {
        String NEWS_URL = "https://n.news.naver.com/article/";
        Set<String> filter = new HashSet<>();
        Document doc = Jsoup.connect(url).get();

        Elements links = doc.select("a");

        List<CrawlingDto> dtos = new ArrayList<>();
        for (Element link : links) {

            if (link.hasClass("list_title") && link.hasClass("nclicks('RBP.rnknws')")) {
                String hrefValue = link.attr("href");
                String imgUrl = link.select("img").attr("src");

                System.out.println(link);

                Document doc2 = Jsoup.connect(hrefValue).get();
                Elements articles = doc2.select("article[id^=dic_area]");
                String test = articles.text();

                dtos.add(CrawlingDto.builder().title(link.text()).url(hrefValue).imgUrl(imgUrl).content(articles.text()).build());
            }

        }
        System.out.println(dtos);
        System.out.println(dtos.size());

    }
    public static void getNews2() throws IOException{
        String NEWS_URL = "https://n.news.naver.com/article/";
        Document doc = Jsoup.connect(url).get();

        Elements test= doc.select("li");
        List<CrawlingDto> dtos = new ArrayList<>();
        Set<String> filter = new HashSet<>();


        for(Element e: test){
            String imgUrl =  e.select("li a.list_img img").attr("data-src");
            String title = e.select("a").text();
            String contentUrl=e.select("li div.list_content a").attr("href");
            System.out.println(contentUrl);
            if(contentUrl.startsWith(NEWS_URL)){
                Document doc2 = Jsoup.connect(contentUrl).get();
                Elements articles = doc2.select("article[id^=dic_area]");
                String content = articles.text();


                if(imgUrl!=""&&!filter.contains(imgUrl)){
                    filter.add(imgUrl);
                    dtos.add(CrawlingDto.builder().title(title).url(contentUrl).imgUrl(imgUrl).content(content).build());

                }
            }


        }
        System.out.println(dtos);
        System.out.println(dtos.size());

    }


}
