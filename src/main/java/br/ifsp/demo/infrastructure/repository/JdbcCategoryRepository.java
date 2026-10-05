package br.ifsp.demo.infrastructure.repository;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.application.event.category.CategoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCategoryRepository implements CategoryRepository {

    private final JdbcTemplate jdbc;

    public JdbcCategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Category save(Category category) {
        jdbc.update(
                "INSERT INTO category (id, event_id, name, capacity, price) VALUES (?, ?, ?, ?, ?)",
                category.getId().toString(),
                category.getEvent().getId().toString(),
                category.getName(),
                category.getCapacity(),
                category.getPrice()
        );
        return category;
    }

    @Override
    public void delete(Category category) {
        jdbc.update("DELETE FROM category WHERE id = ?", category.getId().toString());
    }
}
