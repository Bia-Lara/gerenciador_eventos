package br.ifsp.demo.application.event.category;

import br.ifsp.demo.domain.Category;

public interface CategoryRepository {
    void delete(Category category);
}
