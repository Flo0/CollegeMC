package net.collegemc.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.collegemc.common.gson.GsonSerializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeInferredSerializedUnit<T> {

  private Class<T> elementType;
  private String json;

  public T translate(GsonSerializer serializer) {
    return serializer.fromJson(json, elementType);
  }

}
