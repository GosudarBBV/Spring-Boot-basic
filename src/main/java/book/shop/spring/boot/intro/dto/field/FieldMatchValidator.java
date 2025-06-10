package book.shop.spring.boot.intro.dto.field;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.beanutils.BeanUtils;

public class FieldMatchValidator implements ConstraintValidator<FieldMatch, Object> {
    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(FieldMatch constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Object first = BeanUtils.getProperty(value, firstFieldName);
            Object second = BeanUtils.getProperty(value, secondFieldName);
            return Objects.equals(first, second);
        } catch (Exception e) {
            return false;
        }
    }
}
