package net.collegemc.common.database.redis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import lombok.RequiredArgsConstructor;
import net.collegemc.common.gson.GsonSerializer;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;

@RequiredArgsConstructor
public class GsonEncoder implements Encoder {

  private final GsonSerializer gsonSerializer;

  @Override
  public ByteBuf encode(Object in) throws IOException {
    ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
    try (ByteBufOutputStream os = new ByteBufOutputStream(out)) {
      os.writeUTF(gsonSerializer.toJson(in));
      os.writeUTF(in.getClass().getName());
      return os.buffer();
    } catch (IOException e) {
      out.release();
      throw e;
    } catch (Exception e) {
      out.release();
      throw new IOException(e);
    }
  }
}
