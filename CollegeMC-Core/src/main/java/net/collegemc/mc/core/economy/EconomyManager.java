package net.collegemc.mc.core.economy;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.database.mongodb.MongoMap;
import net.collegemc.common.model.AutoSynchronizedGlobalDataMap;
import net.collegemc.common.model.DataMapContext;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.ServerConfigurationService;
import net.collegemc.mc.libs.tasks.TaskManager;

import java.util.UUID;

public class EconomyManager {

  public static final String NAMESPACE_ACCOUNTS = "Economy-Accounts";
  public static final String NAMESPACE_TRANSACTIONS = "Economy-Transactions";

  private final AutoSynchronizedGlobalDataMap<UUID, EconomyAccount> accounts;
  private final MongoMap<UUID, EconomyTransactionEvent> transactions;

  public EconomyManager() {
    DataMapContext<UUID, EconomyAccount> context = DataMapContext.<UUID, EconomyAccount>mapContextBuilder()
            .creator(key -> new EconomyAccount(new ProfileId(key)))
            .namespace(NAMESPACE_ACCOUNTS)
            .keyClass(UUID.class)
            .valueClass(EconomyAccount.class)
            .mongoDatabase(GlobalGateway.getMongoClient().getDatabase(GlobalGateway.DATABASE_NAME))
            .redissonClient(GlobalGateway.getRedissonClient())
            .serializer(CollegeLibrary.getGsonSerializer())
            .build();

    this.accounts = GlobalGateway.getDataDomainManager().getOrCreateAutoSyncDataDomain(context);

    MongoDatabase database = GlobalGateway.getMongoClient().getDatabase(GlobalGateway.DATABASE_NAME);
    MongoCollection<EconomyTransactionEvent> collection = database.getCollection(NAMESPACE_TRANSACTIONS, EconomyTransactionEvent.class);
    this.transactions = new MongoMap<>(collection, CollegeLibrary.getGsonSerializer(), UUID.class);

    if (CollegeLibrary.getDebugLevel() == ServerConfigurationService.DebugLevel.HIGH) {
      accounts.registerListener("__debug", (key, value) -> CollegeLibrary.info("EconomyAccount cache changed: " + key));
    }
  }

  public void cacheAccount(ProfileId profileId) {
    this.accounts.enableLocalCacheFor(profileId.getUid());
  }

  public void uncacheAccount(ProfileId profileId) {
    this.accounts.disableLocalCacheFor(profileId.getUid());
  }

  public EconomyAccount getAccountSnapshot(ProfileId profileId) {
    return this.accounts.getCachedValue(profileId.getUid());
  }

  public EconomyTransactionResult applyTransaction(EconomyTransaction transaction) {
    EconomyTransactionResult result = this.accounts.compute(transaction.getTargetId().getUid(), transaction::applyTo);
    TaskManager.runOnIOPool(() -> {
      ProfileId targetId = transaction.getTargetId();
      long now = System.currentTimeMillis();
      UUID transactionId = transaction.getTransactionId();
      EconomyOperation operation = transaction.getOperation();
      double amount = transaction.getAmount();
      EconomyTransactionEvent event = new EconomyTransactionEvent(targetId, transactionId, now, result, operation, amount);
      this.transactions.put(transactionId, event);
    });
    return result;
  }

}
