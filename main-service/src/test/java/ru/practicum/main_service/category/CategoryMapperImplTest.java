package ru.practicum.main_service.category;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main_service.category.dto.CategoryDto;
import ru.practicum.main_service.category.dto.NewCategoryDto;
import ru.practicum.main_service.category.mapper.CategoryMapperImpl;
import ru.practicum.main_service.category.model.Category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class CategoryMapperImplTest {
    @InjectMocks
    private CategoryMapperImpl categoryMapper;

    private final NewCategoryDto newCategoryDto = NewCategoryDto.builder()
            .name("Test category 1")
            .build();
    private final Category category = Category.builder()
            .id(1L)
            .name(newCategoryDto.getName())
            .build();
    private final CategoryDto categoryDto = CategoryDto.builder()
            .id(category.getId())
            .name(category.getName())
            .build();

    @Nested
    class NewCategoryDtoToCategory {
        @Test
        public void shouldReturnCategory() {
            Category result = categoryMapper.newCategoryDtoToCategory(newCategoryDto);

            assertNull(result.getId());
            assertEquals(newCategoryDto.getName(), result.getName());
        }

        @Test
        public void shouldReturnNull() {
            Category result = categoryMapper.newCategoryDtoToCategory(null);

            assertNull(result);
        }
    }

    @Nested
    class CategoryDtoToCategory {
        @Test
        public void shouldReturnCategory() {
            Category result = categoryMapper.categoryDtoToCategory(categoryDto);

            assertEquals(categoryDto.getId(), result.getId());
            assertEquals(categoryDto.getName(), result.getName());
        }

        @Test
        public void shouldReturnNull() {
            Category result = categoryMapper.categoryDtoToCategory(null);

            assertNull(result);
        }
    }

    @Nested
    class ToCategoryDto {
        @Test
        public void shouldReturnCategoryDto() {
            CategoryDto result = categoryMapper.toCategoryDto(category);

            assertEquals(categoryDto.getId(), result.getId());
            assertEquals(categoryDto.getName(), result.getName());
        }

        @Test
        public void shouldReturnNull() {
            CategoryDto result = categoryMapper.toCategoryDto(null);

            assertNull(result);
        }
    }
}
