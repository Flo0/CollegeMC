package net.collegemc.common.database.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;


public class InfluxManager {

  private final static String dbURL = "http://localhost:8086";
  private final static String token = "GENERATE_SECRET_TOKEN";
  private final static String bucket = "collegemc";
  private final static String org = "collegemc";
  private InfluxDBClient client;

  public void initiate() {
    client = InfluxDBClientFactory.create(dbURL, token.toCharArray());

    // EXAMPLE POINT
    Point point = Point
            .measurement("mem")
            .addTag("host", "host1")
            .addField("used_percent", 23.43234543)
            .time(Instant.now(), WritePrecision.NS);

    WriteApiBlocking writeApi = client.getWriteApiBlocking();
    writeApi.writePoint(bucket, org, point);
  }

}
