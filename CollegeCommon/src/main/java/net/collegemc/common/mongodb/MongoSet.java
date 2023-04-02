package net.collegemc.common.mongodb;

import com.google.common.base.Preconditions;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import net.collegemc.common.gson.GsonSerializer;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MongoSet<E extends Identity<?>> implements Set<E> {

  private final MongoCollection<E> mongoBackbone;

  public MongoSet(MongoCollection<E> mongoBackbone, GsonSerializer gsonSerializer) {
    Preconditions.checkArgument(mongoBackbone != null && gsonSerializer != null);

    CodecRegistry codec = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), gsonSerializer.createCodecRegistry());
    this.mongoBackbone = mongoBackbone.withCodecRegistry(codec);
  }

  @Override
  public int size() {
    return (int) this.mongoBackbone.countDocuments();
  }

  @Override
  public boolean isEmpty() {
    return this.size() == 0;
  }

  @Override
  public boolean contains(Object object) {
    if (!(object instanceof Identity<?> identity)) {
      return false;
    }
    Bson filter = Filters.eq(identity.getIdentity());
    return this.mongoBackbone.find(filter).first() != null;
  }

  @NotNull
  @Override
  public Iterator<E> iterator() {
    return this.mongoBackbone.find().iterator();
  }

  @NotNull
  @Override
  public Object[] toArray() {
    List<Object> objects = new ArrayList<>();
    this.mongoBackbone.find().into(objects);
    return objects.toArray();
  }

  @NotNull
  @Override
  public <T> T[] toArray(@NotNull T[] array) {
    List<Object> objects = new ArrayList<>();
    this.mongoBackbone.find().into(objects);
    return objects.toArray(array);
  }

  @Override
  public boolean add(E element) {
    ReplaceOptions options = new ReplaceOptions().upsert(true);
    Bson filter = Filters.eq(element.getIdentity());
    UpdateResult result = this.mongoBackbone.replaceOne(filter, element, options);
    return result.getModifiedCount() != 0;
  }

  @Override
  public boolean remove(Object object) {
    if (!(object instanceof Identity<?> identity)) {
      return false;
    }
    Bson filter = Filters.eq(identity.getIdentity());
    return this.mongoBackbone.deleteOne(filter).getDeletedCount() != 0;
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> collection) {
    for (Object object : collection) {
      if (!this.contains(object)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends E> collection) {
    boolean allAdded = true;

    for (E element : collection) {
      if (!this.add(element)) {
        allAdded = false;
      }
    }

    return allAdded;
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> collection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> collection) {
    boolean removedAll = true;

    for (Object object : collection) {
      if (!this.remove(object)) {
        removedAll = false;
      }
    }

    return removedAll;
  }

  @Override
  public void clear() {
    this.mongoBackbone.drop();
  }

}
