package com.eazygo.demo.Controller;

import com.eazygo.demo.Entities.PassengersDTO;
import com.eazygo.demo.Services.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController 
{

    @Autowired
    private DriverService driverService;

    @GetMapping("/passengers-today")
    public ResponseEntity<List<PassengersDTO>> getPassengersToday() 
    {
        return ResponseEntity.ok(driverService.getConfirmedPassengersToday());
    }
}