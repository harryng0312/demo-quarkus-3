package org.harryng.demo;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.GenericTransactionManagerLookup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class AnnotationConfigInfinispanServerTest {
    static Logger logger = LoggerFactory.getLogger(AnnotationConfigInfinispanServerTest.class);

    @ConfigProperty(name = "cache.host")
    protected String localhost;

    @ConfigProperty(name = "cache.name-test")
    protected String CACHE_TEST_NAME;
    @Inject
    protected RemoteCacheManager remoteCacheManager;
    @Inject
    @Remote("distCache")
    protected RemoteCache<String, String> distCache;
    @Inject
    @Remote("testCache")
    protected RemoteCache<String, String> testCache;

    @Inject
    @Remote("respCache")
    protected RemoteCache<String, String> respCache;

    @BeforeAll
    public static void initAll() {

    }

    @AfterAll
    public static void destroy() {
    }

    @BeforeEach
    public void init() {
        logger.info("testCache is transactional: {}", remoteCacheManager.isTransactional(CACHE_TEST_NAME));
    }

    @Test
    public void testReadWriteCache() throws InterruptedException {
        logger.info("hello");
        distCache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
        Thread.sleep(1_000);
        String value = distCache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }

    @Test
    public void testCreateCache() throws InterruptedException {
//        RemoteCache<String, String> cache = remoteCacheManager.getCache("testCache");
        logger.info("is transaction:{}", testCache.isTransactional());
        testCache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
        Thread.sleep(1_000);
        String value = testCache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }

    @Test
    public void testReplicatedCache() throws InterruptedException {
        logger.info("is transaction:{}", respCache.isTransactional());
        respCache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
        Thread.sleep(1_000);
        String value = respCache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }

    @Test
    public void testCreateTransactionalCache() throws InterruptedException, SystemException, NotSupportedException {
//        RemoteCache<String, String> cache = remoteCacheManager.getCache("testCache");
        RemoteCache<String, String> testCache = remoteCacheManager.getCache(CACHE_TEST_NAME,
                TransactionMode.FULL_XA, GenericTransactionManagerLookup.getInstance().getTransactionManager());
        TransactionManager transactionManager = testCache.getTransactionManager();
        try {
            transactionManager.begin();
            logger.info("is transaction:{}", testCache.isTransactional());
            testCache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
            Thread.sleep(1_000);
            transactionManager.commit();
        } catch (HeuristicRollbackException | RollbackException | HeuristicMixedException e) {
            transactionManager.rollback();
            logger.error("Rollback!!!", e);
        }
        String value = testCache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }
}