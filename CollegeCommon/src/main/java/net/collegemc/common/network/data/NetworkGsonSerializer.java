package net.collegemc.common.network.data;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.gson.adapters.ClassAdapter;
import net.collegemc.common.gson.adapters.UUIDAdapter;

import java.util.UUID;

public class NetworkGsonSerializer extends GsonSerializer {

  public NetworkGsonSerializer() {
    this.registerTypeAdapter(Class.class, new ClassAdapter());
    this.registerTypeAdapter(UUID.class, new UUIDAdapter());
  }

}
