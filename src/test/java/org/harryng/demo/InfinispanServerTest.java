package org.harryng.demo;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.configuration.BasicConfiguration;
import org.infinispan.commons.configuration.StringConfiguration;
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
        final var clusterName = ConfigProvider.getConfig().getValue("cache.cluster-name", String.class);
        final var username = ConfigProvider.getConfig().getValue("quarkus.infinispan-client.username", String.class);
        final var passwd = ConfigProvider.getConfig().getValue("quarkus.infinispan-client.password", String.class);
        final var configStr = """
                <?xml version="1.0"?>
                <distributed-cache name="distCache" owners="2" mode="SYNC" statistics="true">
                \t<encoding media-type="application/x-protostream"/>
                \t<locking isolation="READ_COMMITTED"/>
                \t<transaction mode="NON_XA" locking="PESSIMISTIC"/>
                \t<memory storage="HEAP"/>
                \t<persistence passivation="true">
                \t\t<file-store>
                \t\t\t<data path="./cache"/>
                \t\t\t<index path="./index"/>
                \t\t</file-store>
                \t</persistence>
                </distributed-cache>""";
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
        final RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
        BasicConfiguration basicConfiguration = new StringConfiguration(configStr);
        final RemoteCache<String, String> cache = cacheManager.administration().getOrCreateCache("testCache", basicConfiguration);
//        RemoteCache<String, String> cache = cacheManager.getCache("testCache");
        cache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
        Thread.sleep(1_000);
        String value = cache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }


}