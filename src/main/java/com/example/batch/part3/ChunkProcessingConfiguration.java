package com.example.batch.part3;


import antlr.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class ChunkProcessingConfiguration {


        private final JobBuilderFactory jobBuilderFactory;
        private final StepBuilderFactory stepBuilderFactory;

    public ChunkProcessingConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job chunkProcessingJob(){
        return jobBuilderFactory.get("chunkProcessingJob")
                .incrementer(new RunIdIncrementer())
                .start(this.taskbaseStep())
                .next(this.chunkBaseStep(null))
                .build();
    }

    @Bean
    public Step taskbaseStep(){
        return stepBuilderFactory.get("taskBaseStep")
                .tasklet(this.tasklet())
                .build();
    }



    private Tasklet tasklet(){
        return ((contribution, chunkContext) -> {
//            StepExecution stepExecution = contribution.getStepExecution();
//            JobParameters jobParameters = stepExecution.getJobParameters();
//            String value = jobParameters.getString("chunkSize", "10");
            List<String> items = getItems();
            log.info("task item size : {}", items.size());
            return RepeatStatus.FINISHED;
        });
    }

    private List<String> getItems(){
        List<String> items = new ArrayList<>();
        for(int i=0; i<100; i++){
            items.add(i+"hello");
        }
        return items;
    }

    @Bean
    @JobScope
    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize){
        int size = Integer.valueOf(chunkSize);
        return stepBuilderFactory.get("chunkBaseStep")
                //읽어들이는 데이터 자료형 , 반환하는 데이터 자료형
                .<String, String>chunk(size)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<? super String> itemWriter() {
        return items -> log.info("{}", items.size());
//        return items -> items.forEach(log::info);
    }

    //여기서 null로 반환될 경우 아예 안넘어간다.
    private ItemProcessor<String, String> itemProcessor() {
        return item -> item + ", spring batch";
    }

    private ItemReader<String> itemReader() {
        //생성자로 리스트를 받는다.
        return new ListItemReader<>(getItems());
    }





}
