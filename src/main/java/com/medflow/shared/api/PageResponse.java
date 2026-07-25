package com.medflow.shared.api;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;

/** Stable pagination envelope decoupling API consumers from Spring Data's {@link Page}. */
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages) implements Serializable {

  public static <T> PageResponse<T> from(Page<T> page) {
    return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
        page.getTotalElements(), page.getTotalPages());
  }
}
