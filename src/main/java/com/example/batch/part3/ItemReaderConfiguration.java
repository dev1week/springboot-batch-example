package com.example.batch.part3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final DataSource dataSource;

    public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job itemReaderJob() throws Exception {
        return this.jobBuilderFactory.get("itemReaderJob")
                .incrementer(new RunIdIncrementer())
                .start(this.customItemReaderStep())
                //csv 파일을 읽는 step을 job에 추가함
                .next(this.csvFileStep())
                .next(this.jdbcStep())
                .build();
    }

    @Bean
    public Step customItemReaderStep() {
        return this.stepBuilderFactory.get("customItemReaderStep")
                .<Person, Person>chunk(10)
                .reader(new CustomItemReader<Person>(getItems()))
                .writer(itemWriter())
                .build();
    }

    private ItemWriter<Person> itemWriter() {

        return items -> log.info(items.stream()
                .map(Person::getName)
                .collect(Collectors.joining(", ")));
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        for(int i=0; i<10; i++){
            items.add(new Person(i+1, "test", "age", "address"));
        }
        return items;
    }


    //CSV파일을 읽어보자
    @Bean
    public Step csvFileStep() throws Exception{
        return stepBuilderFactory.get("csvFileStep")
                .<Person, Person>chunk(10)
                .reader(this.csvFileItemReader())
                .writer(itemWriter())
                .build();
    }


    private FlatFileItemReader<Person> csvFileItemReader() throws Exception {
        //CSV파일을 한줄씩 읽는 객체
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();

        //읽어와서 person 객체 프로퍼티에 맞게끔 토크나이저
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "name", "age", "address");
        lineMapper.setLineTokenizer(tokenizer);

        //PERSON 객체 매핑
        lineMapper.setFieldSetMapper(fieldSet -> {
            int id = fieldSet.readInt("id");
            String name = fieldSet.readString("name");
            String age = fieldSet.readString("age");
            String address = fieldSet.readString("address");

            return new Person(id, name, age, address);
        });

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>().name("csvFileItemReader")
                .encoding("UTF-8")
                //ClassPathResource는 resuorce 폴더 밑에 읽을 파일을 지정할 수 있다.
                .resource(new ClassPathResource("test.csv"))
                //첫째줄 생략 옵션
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();

        itemReader.afterPropertiesSet();

        return itemReader;
    }

    //JDBC 기반으로 만들기
    private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception{
        JdbcCursorItemReader<Person> itemReader= new JdbcCursorItemReaderBuilder<Person>()
                .name("jdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("select id, name, age, address from person")
                //칼럼 인덱스는 1번부터 시작한다. ㄴ
                .rowMapper((rs, rowNum)->new Person(
                        rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)
                )).build();
        itemReader.afterPropertiesSet();

        return itemReader;
    }

    @Bean
    public Step jdbcStep() throws Exception{
        return stepBuilderFactory.get("jdbcStep")
                .<Person, Person>chunk(10)
                .reader(jdbcCursorItemReader())
                .writer(itemWriter())
                .build();
    }

}
