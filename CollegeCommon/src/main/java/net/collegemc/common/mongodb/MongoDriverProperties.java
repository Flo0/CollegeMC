package net.collegemc.common.mongodb;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MongoDriverProperties {

  private String user;
  private String password;
  private String hostAddress;
  private int hostPort;

}
