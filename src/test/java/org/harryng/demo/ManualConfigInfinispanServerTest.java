package org.harryng.demo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.*;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
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
public class ManualConfigInfinispanServerTest {
    static Logger logger = LoggerFactory.getLogger(ManualConfigInfinispanServerTest.class);

    @ConfigProperty(name = "cache.host")
    protected String localhost;

    protected static RemoteCacheManager remoteCacheManager = null;
    protected static String CACHE_TEST_NAME = ConfigProvider.getConfig().getValue("cache.name-test", String.class);
    protected static String CACHE_DIST_NAME = ConfigProvider.getConfig().getValue("cache.name-dist", String.class);
    protected RemoteCache<String, String> testCache = null;
    protected RemoteCache<String, String> distCache = null;

    @BeforeAll
    public static void initAll() {
        if (remoteCacheManager == null) {
            final var clusterName = ConfigProvider.getConfig().getValue("cache.cluster-name", String.class);
            final var username = ConfigProvider.getConfig().getValue("quarkus.infinispan-client.username", String.class);
            final var passwd = ConfigProvider.getConfig().getValue("quarkus.infinispan-client.password", String.class);
            final var localhost = ConfigProvider.getConfig().getValue("cache.host", String.class);
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            final var configStr = "<?xml version=\"1.0\"?>\n" +
                    "<distributed-cache name=\"" + CACHE_TEST_NAME +
                    "\" owners=\"2\" mode=\"SYNC\" statistics=\"true\">\n" +
                    "\t<encoding media-type=\"application/x-java-object\"/>\n" +
                    "\t<locking isolation=\"REPEATABLE_READ\"/>\n" +
                    "\t<transaction mode=\"FULL_XA\" locking=\"PESSIMISTIC\"/>\n" +
                    "\t<expiration lifespan=\"3600000\" max-idle=\"-1\"/>\n" +
                    "\t<persistence passivation=\"false\">\n" +
                    "\t\t<file-store>\n" +
                    "\t\t\t<data path=\"data\"/>\n" +
                    "\t\t\t<index path=\"index\"/>\n" +
                    "\t\t</file-store>\n" +
                    "\t</persistence>\n" +
                    "</distributed-cache>";
//            final BasicConfiguration basicConfiguration = new StringConfiguration(configStr);
//        builder
//                .addServer().host(localhost).port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
//                .addServer().host(localhost).port(ConfigurationProperties.DEFAULT_HOTROD_PORT + 50)
//                .addServer().host(localhost).port(ConfigurationProperties.DEFAULT_HOTROD_PORT + 100)
//                .security().authentication()
//                .username(username)
//                .password(passwd);
//                .realm("default")
//                .saslMechanism("SCRAM-SHA-512");
            builder.addCluster(clusterName)
                    .addClusterNode(localhost, ConfigurationProperties.DEFAULT_HOTROD_PORT)
                    .addClusterNode(localhost, ConfigurationProperties.DEFAULT_HOTROD_PORT + 50)
                    .addClusterNode(localhost, ConfigurationProperties.DEFAULT_HOTROD_PORT + 100)
                    .transaction().transactionTimeout(60, TimeUnit.SECONDS)
                    .security().authentication()
                    .username(username).password(passwd);
            builder.remoteCache(CACHE_TEST_NAME)
                    .configuration(configStr)
                    // remote transaction config
                    .transactionManagerLookup(GenericTransactionManagerLookup.getInstance())
                    .transactionMode(TransactionMode.FULL_XA);
            builder.remoteCache(CACHE_DIST_NAME);
            remoteCacheManager = new RemoteCacheManager(builder.build());
            logger.info("Started the Remote Cache Manager");
        }
    }

    @AfterAll
    public static void destroy() {
        remoteCacheManager.close();
        logger.info("Destroyed the Remote Cache Manager");
    }

    @BeforeEach
    public void init() {
//        final var configStr = "<?xml version=\"1.0\"?>\n" +
//                "<distributed-cache name=\"" + ConfigProvider.getConfig().getValue("cache.name", String.class) +
//                "\" owners=\"2\" mode=\"SYNC\" statistics=\"true\">\n" +
//                "\t<encoding media-type=\"application/x-java-object\"/>\n" +
//                "\t<locking isolation=\"REPEATABLE_READ\"/>\n" +
//                "\t<transaction mode=\"FULL_XA\" locking=\"PESSIMISTIC\"/>\n" +
//                "\t<expiration lifespan=\"3600000\" max-idle=\"-1\"/>\n" +
//                "\t<persistence passivation=\"false\">\n" +
//                "\t\t<file-store>\n" +
//                "\t\t\t<data path=\"data\"/>\n" +
//                "\t\t\t<index path=\"index\"/>\n" +
//                "\t\t</file-store>\n" +
//                "\t</persistence>\n" +
//                "</distributed-cache>";
//        final BasicConfiguration basicConfiguration = new StringConfiguration(configStr);
//        testCache = remoteCacheManager.administration().getOrCreateCache(ConfigProvider.getConfig()
//                .getValue("cache.name", String.class), basicConfiguration);
        testCache = remoteCacheManager.getCache(CACHE_TEST_NAME);
        distCache = remoteCacheManager.getCache(CACHE_DIST_NAME);
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
    public void testCreateTransactionalCache() throws InterruptedException, SystemException, NotSupportedException {
//        RemoteCache<String, String> cache = remoteCacheManager.getCache("testCache");
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