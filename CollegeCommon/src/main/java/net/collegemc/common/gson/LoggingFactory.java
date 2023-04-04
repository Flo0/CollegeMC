package net.collegemc.common.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class LoggingFactory implements TypeAdapterFactory {

  @Getter
  @Setter
  private boolean loggingEnabled = false;

  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
    TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);

    return new TypeAdapter<>() {
      @Override
      public void write(JsonWriter jsonWriter, T object) throws IOException {
        if (loggingEnabled && object != null) {
          System.out.println("Writing " + object.getClass().getSimpleName());
        }
        delegate.write(jsonWriter, object);
      }

      @Override
      public T read(JsonReader jsonReader) throws IOException {
        T object = delegate.read(jsonReader);
        if (loggingEnabled && object != null) {
          System.out.println("Read " + object.getClass().getSimpleName());
        }
        return object;
      }
    };
  }
}
