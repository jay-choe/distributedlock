package com.example.distributedlock.service;

import com.example.distributedlock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository repository;

    public void increaseStockFrom(long id, long qty) {

        repository.findById(id)
            .ifPresent(stock -> {
                stock.increase(qty);
                repository.saveAndFlush(stock);
            });
    }
}
