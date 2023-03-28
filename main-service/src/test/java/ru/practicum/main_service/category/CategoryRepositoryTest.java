package ru.practicum.main_service.category;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.main_service.category.model.Category;
import ru.practicum.main_service.category.repository.CategoryRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CategoryRepositoryTest {
    private final CategoryRepository categoryRepository;
    private final Category category1 = Category.builder()
            .id(1L)
            .name("test category 1")
            .build();
    private final Category category2 = Category.builder()
            .id(2L)
            .name("test category 1")
            .build();

    @BeforeEach
    public void beforeEach() {
        categoryRepository.save(category1);
    }

    @Nested
    class Save {
        @Test
        public void shouldThrowExceptionIfNameExist() {
            assertThrows(DataIntegrityViolationException.class, () -> categoryRepository.save(category2));

            assertTrue(categoryRepository.findById(category2.getId()).isEmpty());
        }
    }
}
