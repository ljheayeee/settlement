package com.settlement.project.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class BatchProcessor {
    public static final int BATCH_SIZE = 500;

    public static <T> void saveBatch(List<T> items, Consumer<List<T>> saveFunction) {
        saveBatch(items, saveFunction, BATCH_SIZE);
    }

    public static <T> void saveBatch(List<T> items, Consumer<List<T>> saveFunction, int batchSize) {
        for (int i = 0; i < items.size(); i += batchSize) {
            List<T> batch = items.subList(i, Math.min(items.size(), i + batchSize));
            saveFunction.accept(batch);
        }
    }
}