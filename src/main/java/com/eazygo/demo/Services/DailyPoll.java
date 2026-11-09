package com.eazygo.demo.Services;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class DailyPoll {
    private LocalDate data; //data da enquete

    public DailyPoll(LocalDate data){
        this.data = data;
    }
}
