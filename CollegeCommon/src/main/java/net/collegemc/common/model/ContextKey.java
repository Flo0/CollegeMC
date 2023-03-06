package net.collegemc.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class ContextKey<K, V> {

  protected final String namespace;
  protected final Class<K> keyClass;
  protected final Class<V> valueClass;

  @Override
  public int hashCode() {
    return Objects.hash(this.namespace, this.keyClass, this.valueClass);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ContextKey key) {
      return this.namespace.equals(key.namespace) && this.keyClass.equals(key.keyClass) && this.valueClass.equals(key.valueClass);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "{namespace:" + namespace + ", key:" + keyClass.getName() + ", value: " + valueClass.getName() + "}";
  }
}
