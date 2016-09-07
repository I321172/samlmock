package saml.response.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil
{
    private static final Map<Class<?>, Field[]>  declaredFieldsCache  = new ConcurrentHashMap<Class<?>, Field[]>(256);
    private static final Field[]                 NO_FIELDS            = {};

    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<Class<?>, Method[]>(256);
    private static final Method[]                NO_METHODS           = {};

    public static Field findField(Class<?> clazz, String name)
    {
        return findField(clazz, name, null);
    }

    public static Field findField(Class<?> clazz, String name, Class<?> type)
    {
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null)
        {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields)
            {
                if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType())))
                {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static Field[] getDeclaredFields(Class<?> clazz)
    {
        Field[] result = declaredFieldsCache.get(clazz);
        if (result == null)
        {
            result = clazz.getDeclaredFields();
            declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
        }
        return result;
    }

    public static Method findMethod(Class<?> clazz, String name)
    {
        return findMethod(clazz, name, new Class<?>[0]);
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied
     * name and parameter types. Searches all superclasses up to {@code Object}.
     * <p>
     * Returns {@code null} if no {@link Method} can be found.
     * 
     * @param clazz
     *            the class to introspect
     * @param name
     *            the name of the method
     * @param paramTypes
     *            the parameter types of the method (may be {@code null} to
     *            indicate any signature)
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes)
    {

        Class<?> searchType = clazz;
        while (searchType != null)
        {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
            for (Method method : methods)
            {
                if (name.equals(method.getName())
                        && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes())))
                {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static Object invokeMethod(Method method, Object target, Object... args)
    {
        try
        {
            return method.invoke(target, args);
        } catch (Exception ex)
        {
            System.out.print(ex.getMessage());
            return null;
        }
    }

    public static boolean setField(Field field, Object target, Object value)
    {
        boolean isSuccess = true;
        try
        {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException ex)
        {
            isSuccess = false;
            System.out.println(ex.getMessage());
        } catch (IllegalArgumentException e)
        {
            isSuccess = false;
            // System.out.println(e.getMessage());
        }
        return isSuccess;
    }

    /**
     * This variant retrieves {@link Class#getDeclaredMethods()} from a local
     * cache in order to avoid the JVM's SecurityManager check and defensive
     * array copying. In addition, it also includes Java 8 default methods from
     * locally implemented interfaces, since those are effectively to be treated
     * just like declared methods.
     * 
     * @param clazz
     *            the class to introspect
     * @return the cached array of methods
     * @see Class#getDeclaredMethods()
     */
    private static Method[] getDeclaredMethods(Class<?> clazz)
    {
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null)
        {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null)
            {
                result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods)
                {
                    result[index] = defaultMethod;
                    index++;
                }
            } else
            {
                result = declaredMethods;
            }
            declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
        }
        return result;
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz)
    {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces())
        {
            for (Method ifcMethod : ifc.getMethods())
            {
                if (!Modifier.isAbstract(ifcMethod.getModifiers()))
                {
                    if (result == null)
                    {
                        result = new LinkedList<Method>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

}
