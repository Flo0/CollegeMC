package net.collegemc.common.mongodb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.collegemc.common.gson.GsonSerializer;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.BiConsumer;

public class MongoGsonCodec<V> implements Codec<V> {

  private static final Map<Class<? extends Number>, BiConsumer<Number, BsonWriter>> NUM_WRITERS = Map.of(
          Double.class, (num, writer) -> writer.writeDouble(num.doubleValue()),
          Integer.class, (num, writer) -> writer.writeInt32(num.intValue()),
          Long.class, (num, writer) -> writer.writeInt64(num.longValue()),
          Float.class, (num, writer) -> writer.writeDouble(num.floatValue()),
          Short.class, (num, writer) -> writer.writeDouble(num.shortValue()),
          Byte.class, (num, writer) -> writer.writeDouble(num.byteValue()),
          BigInteger.class, (num, writer) -> writer.writeString(num.toString()),
          BigDecimal.class, (num, writer) -> writer.writeDecimal128(new Decimal128((BigDecimal) num))
  );

  private final Class<V> typeClass;
  private final GsonSerializer gsonSerializer;

  public MongoGsonCodec(Class<V> typeClass, GsonSerializer gsonSerializer) {
    this.typeClass = typeClass;
    this.gsonSerializer = gsonSerializer;
  }

  @Override
  public Class<V> getEncoderClass() {
    return typeClass;
  }

  @Override
  public V decode(BsonReader reader, DecoderContext decoderContext) {
    JsonObject rootObject = readObject(reader);
    return gsonSerializer.fromJson(rootObject.toString(), this.typeClass);
  }

  @Override
  public void encode(BsonWriter writer, V value, EncoderContext encoderContext) {
    JsonElement jsonElement = gsonSerializer.toJsonTree(value);
    writeJsonElement(writer, jsonElement);
  }

  private void writeJsonElement(BsonWriter writer, JsonElement element) {
    if (element.isJsonObject()) {
      writer.writeStartDocument();

      element.getAsJsonObject().asMap().forEach((key, value) -> {
        writer.writeName(key);
        writeJsonElement(writer, value);
      });

      writer.writeEndDocument();
    } else if (element.isJsonPrimitive()) {
      JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
      if (jsonPrimitive.isString()) {
        writer.writeString(jsonPrimitive.getAsString());
      } else {
        Number jsonNumber = jsonPrimitive.getAsNumber();
        NUM_WRITERS.get(jsonNumber.getClass()).accept(jsonNumber, writer);
      }
    } else if (element.isJsonNull()) {
      writer.writeNull();
    } else if (element.isJsonArray()) {
      writer.writeStartArray();
      element.getAsJsonArray().forEach(value -> writeJsonElement(writer, value));
      writer.writeEndArray();
    } else {
      throw new IllegalStateException("Unidentified json type");
    }
  }

  private JsonObject readObject(BsonReader reader) {
    JsonObject object = new JsonObject();
    reader.readStartDocument();

    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      String key = reader.readName();
      object.add(key, readElement(reader));
    }

    reader.readEndDocument();
    return object;
  }

  private JsonElement readElement(BsonReader reader) {
    return switch (reader.readBsonType()) {
      case DOUBLE -> new JsonPrimitive(reader.readDouble());
      case STRING -> new JsonPrimitive(reader.readString());
      case DOCUMENT -> readObject(reader);
      case ARRAY -> readArray(reader);
      case BOOLEAN -> new JsonPrimitive(reader.readBoolean());
      case INT32 -> new JsonPrimitive(reader.readInt32());
      case INT64 -> new JsonPrimitive(reader.readInt64());
      case DECIMAL128 -> new JsonPrimitive(reader.readDecimal128());
      case NULL -> {
        reader.readNull();
        yield JsonNull.INSTANCE;
      }
      default -> {
        reader.skipValue();
        yield JsonNull.INSTANCE;
      }
    };
  }

  private JsonArray readArray(BsonReader reader) {
    JsonArray jsonArray = new JsonArray();
    reader.readStartArray();

    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      jsonArray.add(readElement(reader));
    }

    reader.readEndArray();
    return jsonArray;
  }

}
