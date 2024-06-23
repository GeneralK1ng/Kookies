package org.kookies.mirai.commen.adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author General_K1ng
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return LocalDate.parse(jsonElement.getAsString(), FORMATTER );
    }

    @Override
    public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(localDate.format(FORMATTER));
    }
}
