package org.harryng.demo;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.configuration.BasicConfiguration;
import org.infinispan.commons.configuration.StringConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class InfinispanServerTest {
    static Logger logger = LoggerFactory.getLogger(InfinispanServerTest.class);

    @ConfigProperty(name = "cache.host")
    protected String localhost;

    @Inject
    @Remote("distCache")
    protected RemoteCache<String, String> distCache;
    protected static RemoteCacheManager remoteCacheManager = null;
    protected RemoteCache<String, String> testCache = null;

    @BeforeAll
    public static void initAll() {
        if (remoteCacheManager == null) {
            final var clusterName = ConfigProvider.getConfig().getValue("cache.cluster-name", String.class);
            final var username = ConfigProvider.getConfig().getValue("quarkus.infinispan-client.username", String.class);
            final var passwd = ConfigProvider.getConfig().getValue("quarkus.infinispan-client.password", String.class);
            final var localhost = ConfigProvider.getConfig().getValue("cache.host", String.class);
            final ConfigurationBuilder builder = new ConfigurationBuilder();
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
                    .security().authentication()
                    .username(username).password(passwd);
            remoteCacheManager = new RemoteCacheManager(builder.build());
            logger.info("start the Remote Cache Manager");
        }
    }

    @AfterEach
    public void destroy() {
        logger.info("destroy the Remote Cache Manager");
        remoteCacheManager.close();
    }

    @BeforeEach
    public void init() {
        final var configStr = "<?xml version=\"1.0\"?>\n" +
                "<distributed-cache name=\"distCache\" owners=\"2\" mode=\"SYNC\" statistics=\"true\">\n" +
                "\t<encoding media-type=\"application/x-java-object\"/>\n" +
                "\t<locking isolation=\"READ_COMMITTED\"/>\n" +
                "\t<transaction mode=\"FULL_XA\" locking=\"PESSIMISTIC\"/>\n" +
                "\t<expiration lifespan=\"3600000\" max-idle=\"-1\"/>\n" +
                "\t<persistence passivation=\"false\">\n" +
                "\t\t<file-store>\n" +
                "\t\t\t<data path=\"data\"/>\n" +
                "\t\t\t<index path=\"index\"/>\n" +
                "\t\t</file-store>\n" +
                "\t</persistence>\n" +
                "</distributed-cache>";
        final BasicConfiguration basicConfiguration = new StringConfiguration(configStr);
        testCache = remoteCacheManager.administration().getOrCreateCache("testCache", basicConfiguration);
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
//        RemoteCache<String, String> cache = cacheManager.getCache("testCache");
        logger.info("is transaction:{}",  testCache.isTransactional());
        testCache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
        Thread.sleep(1_000);
        String value = testCache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }
}