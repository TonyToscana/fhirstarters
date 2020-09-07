package ca.uhn.fhir.example;

import java.lang.reflect.ParameterizedType;

public class ClassUtils {
   public static Class getGenericClass(Class clazz) {
      return (Class) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
   }
}
