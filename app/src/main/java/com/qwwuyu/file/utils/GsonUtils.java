package com.qwwuyu.file.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.DateTypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;

public class GsonUtils {
    private static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Void.class, new VoidTypeAdapter())
                .create();
    }

    public static Gson getGson() {
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
    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    /**
     * 获取类泛型第一个参数
     */
    public static Type getActualTypeArgument0(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            return ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }
        return null;
    }

    /**
     * 判断type是否是Void类型
     */
    public static boolean isTypeVoid(Type type) {
        if (type instanceof Class) {
            return Void.class.isAssignableFrom((Class) type);
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

    private static final class VoidTypeAdapter extends TypeAdapter<Void> {
        @Override
        public void write(final JsonWriter out, final Void value) throws IOException {
            out.nullValue();
        }

        @Override
        public Void read(final JsonReader in) throws IOException {
            in.skipValue();
            return null;
        }
    }
}
