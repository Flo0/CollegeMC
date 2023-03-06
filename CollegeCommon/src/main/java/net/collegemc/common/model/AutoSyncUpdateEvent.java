package net.collegemc.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.collegemc.common.bridge.RemoteEvent;
import net.collegemc.common.utils.TypeInferredSerializedUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoSyncUpdateEvent<K, V> implements RemoteEvent {

  private ContextKey<K, V> contextKey;
  private TypeInferredSerializedUnit<K> updatedKey;

}
