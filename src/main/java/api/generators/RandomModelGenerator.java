package api.generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RandomModelGenerator {

    private static final Random RANDOM = new Random();

    public static <T> T generate(Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);

                // Пропускаем синтетические поля (например, jacoco или системные ссылки)
                if (field.isSynthetic()) {
                    continue;
                }

                Object value;
                GeneratingRule rule = field.getAnnotation(GeneratingRule.class);
                if (rule != null) {
                    value = generateFromRegex(rule.regex(), field.getType());
                } else {
                    value = generateRandomValue(field);
                }

                // Записываем значение только если смогли его сгенерировать
                if (value != null) {
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity: " + clazz.getName(), e);
        }
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Object generateRandomValue(Field field) {
        Class<?> type = field.getType();
        String fieldName = field.getName().toLowerCase();

        // ИНТЕГРАЦИЯ С ВАШИМ RandomData: проверяем имена полей
        if (type.equals(String.class) && (fieldName.contains("username") || fieldName.contains("user"))) {
            return RandomData.getUsername();
        }
        if (type.equals(String.class) && fieldName.contains("password")) {
            return RandomData.getPassword();
        }
        // TeamCity entity IDs must start with a latin letter
        if (type.equals(String.class) && fieldName.equals("id")) {
            return RandomData.getId();
        }
        if (type.equals(String.class) && fieldName.equals("name")) {
            return RandomData.getBuildName();
        }

        // Базовые типы
        if (type.equals(String.class)) {
            return UUID.randomUUID().toString().substring(0, 8);
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return RANDOM.nextInt(1000);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return RANDOM.nextLong();
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return RANDOM.nextDouble() * 100;
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            if (field.getName().equals("disabled")) {
                return false;
            }
            return RANDOM.nextBoolean();
        } else if (type.equals(List.class)) {
            return generateRandomList(field);
        } else if (type.equals(Date.class)) {
            return new Date(System.currentTimeMillis() - RANDOM.nextInt(100000000));
        } else {
            // ЗАЩИТА ОТ СТЭКОВЕРФЛОУ: генерируем вложенный объект, только если он лежит в нашем пакете моделей
            if (type.getName().startsWith("api.")) {
                return generate(type);
            }
            return null; // Чужие/системные классы игнорируем
        }
    }

    private static Object generateFromRegex(String regex, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regex);
        String result = rgxGen.generate();
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(result);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(result);
        } else {
            return result;
        }
    }

    private static List<Object> generateRandomList(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType pt) {
            Type actualType = pt.getActualTypeArguments()[0];

            if (actualType instanceof Class<?> actualClass) {

                if (actualClass == String.class) {
                    return List.of(UUID.randomUUID().toString().substring(0, 5),
                            UUID.randomUUID().toString().substring(0, 5));
                }

                // УМНАЯ СГЕНЕРИРОВАННАЯ СТРУКТУРА: если в списке лежат другие модели (например, List<BuildType>)
                if (actualClass.getName().startsWith("api.")) {
                    return List.of(generate(actualClass), generate(actualClass));
                }
            }
        }
        return Collections.emptyList();
    }
}
