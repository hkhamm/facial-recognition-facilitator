package models;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A custom property naming strategy for the Jackson ObjectMapper.
 */
public class MyPropertyNamingStrategy extends PropertyNamingStrategy {
    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return field.getName();
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return convert(method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return convert(method, defaultName);
    }

    private String convert(AnnotatedMethod method, String defaultName) {
        Class<?> clazz = method.getDeclaringClass();
        List<Field> flds = FieldUtils.getAllFieldsList(clazz);
        for (Field fld : flds) {
            if (fld.getName().equalsIgnoreCase(defaultName)) {
                return fld.getName();
            }
        }

        return defaultName;
    }
}
