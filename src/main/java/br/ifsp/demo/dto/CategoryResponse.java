package br.ifsp.demo.dto;

import br.ifsp.demo.domain.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, int capacity, double price) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getCapacity(), c.getPrice());
    }
}
