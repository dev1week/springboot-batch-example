package com.example.batch.part3;


import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.List;

public class CustomItemReader<T> implements ItemReader<T> {

    private final List<T> items;

    public CustomItemReader(List<T> items) {
        this.items = items;
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        //element를 하나씩 꺼내서 제거하는 동시에 반환한다.
        if(!items.isEmpty()){
            return items.remove(0);
        }

        //null을 return 하면 chunk 반복의 끝이다.
        return null;
    }
}
