package net.collegemc.mc.libs.tasks;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.database.mongodb.Identity;
import net.collegemc.common.database.mongodb.MongoSet;
import net.collegemc.mc.libs.CollegeLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MongoBackedSet<E extends Identity<?>> implements Set<E> {

  private final MongoSet<E> remoteSet;
  private final Set<E> localSet;

  public MongoBackedSet(Set<E> localSet, String namespace, Class<E> valueClass) {
    MongoDatabase database = CollegeLibrary.getServerDatabase();
    MongoCollection<E> collection = database.getCollection(namespace, valueClass);
    this.localSet = localSet;
    this.remoteSet = new MongoSet<>(collection, CollegeLibrary.getGsonSerializer());
  }

  public MongoBackedSet(String namespace, Class<E> valueClass) {
    this(new HashSet<>(), namespace, valueClass);
  }

  @Override
  public int size() {
    return this.localSet.size();
  }

  @Override
  public boolean isEmpty() {
    return this.localSet.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return this.localSet.contains(o);
  }

  @NotNull
  @Override
  public Iterator<E> iterator() {
    return this.localSet.iterator();
  }

  @NotNull
  @Override
  public Object[] toArray() {
    return this.localSet.toArray();
  }

  @NotNull
  @Override
  public <T> T[] toArray(@NotNull T[] a) {
    return this.localSet.toArray(a);
  }

  @Override
  public boolean add(E e) {
    boolean added = this.localSet.add(e);
    TaskManager.runOnIOPool(() -> this.remoteSet.add(e));
    return added;
  }

  @Override
  public boolean remove(Object o) {
    boolean removed = this.localSet.remove(o);
    TaskManager.runOnIOPool(() -> this.remoteSet.remove(o));
    return removed;
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c) {
    return this.localSet.containsAll(c);
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends E> c) {
    boolean addedAll = this.localSet.addAll(c);
    TaskManager.runOnIOPool(() -> this.remoteSet.addAll(c));
    return addedAll;
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    boolean removedAll = this.localSet.removeAll(c);
    TaskManager.runOnIOPool(() -> this.remoteSet.removeAll(c));
    return removedAll;
  }

  @Override
  public void clear() {
    this.localSet.clear();
    TaskManager.runOnIOPool(this.remoteSet::clear);
  }

  public void loadDataFromRemote() {
    this.localSet.addAll(this.remoteSet);
  }

  public void saveDataToRemote() {
    this.remoteSet.addAll(this.localSet);
  }
}
