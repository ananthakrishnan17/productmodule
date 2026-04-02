package util;

import com.google.gson.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GsonUtil {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class,
            (JsonSerializer<LocalDateTime>)
            (src, type, ctx) -> src == null
                ? JsonNull.INSTANCE : new JsonPrimitive(src.toString()))
        .registerTypeAdapter(LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>)
            (json, type, ctx) -> LocalDateTime.parse(json.getAsString()))
        .registerTypeAdapter(LocalDate.class,
            (JsonSerializer<LocalDate>)
            (src, type, ctx) -> src == null
                ? JsonNull.INSTANCE : new JsonPrimitive(src.toString()))
        .registerTypeAdapter(LocalDate.class,
            (JsonDeserializer<LocalDate>)
            (json, type, ctx) -> LocalDate.parse(json.getAsString()))
        .create();
}