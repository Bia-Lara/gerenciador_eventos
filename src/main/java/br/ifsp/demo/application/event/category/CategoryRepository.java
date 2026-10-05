package br.ifsp.demo.application.event.category;

import br.ifsp.demo.domain.Category;

public interface CategoryRepository {
    Category save(Category category);
    void delete(Category category);
}
