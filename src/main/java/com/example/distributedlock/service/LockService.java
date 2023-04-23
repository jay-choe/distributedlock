package com.example.distributedlock.service;

public interface LockService {

    boolean lock(final long key);

    boolean unlock(final long key);

    void increaseStock(final long key, final long qty);

}
