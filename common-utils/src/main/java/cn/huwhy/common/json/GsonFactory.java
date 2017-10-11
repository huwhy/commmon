package cn.huwhy.common.json;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import com.google.gson.*;

@Deprecated
public class GsonFactory {

    public static Gson timeStampGson() {
        return new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeAdapter()).create();
    }

    public static Gson dateFormatGson() {
        return new GsonBuilder().registerTypeAdapter(Date.class, new DateTypeFormatAdapter()).create();
    }

    private static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        public JsonElement serialize(Date src, Type arg1, JsonSerializationContext arg2) {
            return new JsonPrimitive(src.getTime());
        }

        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }

            return new Date(json.getAsLong());
        }
    }

    private static class DateTypeFormatAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

        public JsonElement serialize(Date src, Type arg1, JsonSerializationContext arg2) {
            return new JsonPrimitive(DateFormatUtils.format(src, "yyyy/MM/dd HH:mm:ss"));
        }

        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }

            try {
                return DateUtils.parseDate(json.getAsString(), new String[]{"yyyy/MM/dd HH:mm:ss"});
            } catch (ParseException e) {
                throw new JsonParseException("The date should be yyyy/MM/dd HH:mm:ss");
            }
        }
    }
}
