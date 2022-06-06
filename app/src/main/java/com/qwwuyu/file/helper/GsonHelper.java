package com.qwwuyu.file.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import kotlin.Unit;

public class GsonHelper {
    private static Gson gson;

    public static void init(GsonBuilder gsonBuilder) {
        if (gson == null) {
            synchronized (GsonHelper.class) {
                if (gson == null) {
                    gson = gsonBuilder
                            .registerTypeAdapter(Date.class, new DateTypeAdapter())
                            //.registerTypeAdapter(float.class, new FloatTypeAdapter(0f))//处理float返回空字符串
                            //.registerTypeAdapter(Float.class, new FloatTypeAdapter(null))//处理Float返回空字符串
                            //.registerTypeAdapter(JsonDeserializer.class, deserializer)
                            //.addSerializationExclusionStrategy(new Exclusion())
                            //.addDeserializationExclusionStrategy(new Exclusion())
                            .create();
                }
            }
        }
    }

    public static Gson getGson() {
        if (gson == null) {
            synchronized (GsonHelper.class) {
                init(new GsonBuilder());
            }
        }
        return gson;
    }

    /**
     * 使用gson解析json,异常则返回空
     */
    @Nullable
    public static <T> T fromJson(String result, Class<T> clazz) {
        try {
            return getGson().fromJson(result, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static <T> T fromType(String result, Type type) {
        try {
            return getGson().fromJson(result, type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将对象转化为json
     */
    @NotNull
    public static String toJson(@NotNull Object obj) {
        return getGson().toJson(obj);
    }

    /**
     * 获取类泛型第一个参数
     */
    public static Type getActualTypeArgument0(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) superclass).getActualTypeArguments();
            return types.length > 0 ? types[0] : null;
        }
        return null;
    }

    /**
     * 判断type是否是String类型
     */
    public static boolean isTypeString(Type type) {
        if (type instanceof Class) {
            return String.class.isAssignableFrom((Class) type);
        }
        return false;
    }

    /**
     * 判断type是否继承List
     */
    public static boolean isTypeList(Type type) {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return List.class.isAssignableFrom((Class) rawType);
            }
        }
        return false;
    }

    /**
     * 判断type是否是Unit类型
     */
    public static boolean isTypeUnit(Type type) {
        if (type instanceof Class) {
            return Unit.class.isAssignableFrom((Class) type);
        }
        return false;
    }
}
