package net.collegemc.mc.libs.npcs.serializer;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;

public class MultiMapInstanceCreator implements InstanceCreator<Multimap> {
  @Override
  public Multimap createInstance(Type type) {
    return LinkedHashMultimap.create();
  }
}
