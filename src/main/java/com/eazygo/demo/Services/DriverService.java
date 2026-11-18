package com.eazygo.demo.Services;

import com.eazygo.demo.Entities.PassengersDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DriverService 
{

    // Lista mocada de passageiros que confirmaram presença hoje
    private final List<PassengersDTO> confirmedList = new ArrayList<>();

    public DriverService() 
    {
        confirmedList.add(new PassengersDTO(1, "Ana Silva", "https://i.pravatar.cc/150?img=1", "(51) 99999-1111"));
        confirmedList.add(new PassengersDTO(2, "Carlos Souza", "https://i.pravatar.cc/150?img=2", "(67) 67676-6767"));
        confirmedList.add(new PassengersDTO(3, "Mariana Costa", "https://i.pravatar.cc/150?img=3", "(51) 97777-3333"));
        confirmedList.add(new PassengersDTO(5, "Beatriz Oliveira", "https://i.pravatar.cc/150?img=5", "(51) 95555-5555"));
        confirmedList.add(new PassengersDTO(6, "Lucas Fernandes", "https://i.pravatar.cc/150?img=6", "(51) 94444-6666"));

    }

    public List<PassengersDTO> getConfirmedPassengersToday() 
    {
        return confirmedList;
    }
}