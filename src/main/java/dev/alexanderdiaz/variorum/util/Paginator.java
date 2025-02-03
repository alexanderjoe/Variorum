package dev.alexanderdiaz.variorum.util;

import java.util.List;
import lombok.Getter;

@Getter
public class Paginator<T> {

    private final int pageSize;
    private final int totalItems;
    private final List<T> items;

    public Paginator(List<T> items, int pageSize) {
        this.pageSize = pageSize;
        this.totalItems = items.size();
        this.items = items;
    }

    public List<T> getPage(int page) {
        int start = (page - 1) * pageSize;
        if (start >= totalItems) {
            return List.of();
        }
        int end = Math.min(start + pageSize, totalItems);
        return items.subList(start, end);
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalItems / pageSize);
    }
}
