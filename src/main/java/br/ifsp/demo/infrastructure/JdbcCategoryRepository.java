package br.ifsp.demo.infrastructure;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.repository.CategoryRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCategoryRepository implements CategoryRepository {

    private final JdbcTemplate jdbc;

    public JdbcCategoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void delete(Category category) {
        jdbc.update("DELETE FROM category WHERE id = ?", category.getId().toString());
    }
}