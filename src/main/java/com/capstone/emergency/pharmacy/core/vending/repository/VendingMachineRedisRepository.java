package com.capstone.emergency.pharmacy.core.vending.repository;

import com.capstone.emergency.pharmacy.core.error.ApiException;
import com.capstone.emergency.pharmacy.core.error.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class VendingMachineRedisRepository {

    @Value("${redis.machine.lock.prefix}")
    private String lockPrefix;

    private final ValueOperations<String, Object> valueOperations;

    public void lockMachine(Long vendingMachineId, String userId) {
        final var key = lockPrefix + ":" + vendingMachineId;
        final var existing = (String) valueOperations.get(key);

        if (existing != null && !existing.equals(userId)) {
            throw new ForbiddenException(
                    "Vending machine is locked for checkout by another user",
                    ApiException.Reason.MACHINE_ALREADY_LOCKED
            );
        }

        valueOperations.set(key, userId);
        valueOperations.getOperations().expire(key, 2, TimeUnit.MINUTES);
    }

    public void validateMachineLock(Long vendingMachineId, String userId) {
        final var key = lockPrefix + ":" + vendingMachineId;
        final var existing = (String) valueOperations.get(key);

        if (existing == null || !existing.equals(userId)) {
            throw new ForbiddenException(
                    "Vending machine is locked for checkout by another user",
                    ApiException.Reason.MACHINE_ALREADY_LOCKED
            );
        }
    }
}
