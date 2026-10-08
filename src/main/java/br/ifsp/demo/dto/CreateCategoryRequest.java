package br.ifsp.demo.dto;

public record CreateCategoryRequest(
        String name,
        Double price,
        Integer capacity
) {
}
