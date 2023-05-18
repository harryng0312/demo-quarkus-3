package org.harryng.demo;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.configuration.BasicConfiguration;
import org.infinispan.commons.configuration.StringConfiguration;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class InfinispanServerTest {
    static Logger logger = LoggerFactory.getLogger(InfinispanServerTest.class);
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
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addCluster("test-local-cluster")
                .addServer().host("192.168.1.13").port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
                .addServer().host("192.168.1.13").port(ConfigurationProperties.DEFAULT_HOTROD_PORT + 50)
                .addServer().host("192.168.1.13").port(ConfigurationProperties.DEFAULT_HOTROD_PORT + 100)
                .security().authentication()
                .username("infiuser")
                .password("infiuser");
//                .realm("default")
//                .saslMechanism("SCRAM-SHA-512");
        final RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
        BasicConfiguration basicConfiguration = new StringConfiguration("");
        final RemoteCache<String, String> cache = cacheManager.administration().getOrCreateCache("testCache", DefaultTemplate.DIST_ASYNC);
//        RemoteCache<String, String> cache = cacheManager.getCache("testCache");
        cache.put("test key", "test value at: " + LocalDateTime.now(), 5, TimeUnit.MINUTES);
        Thread.sleep(1_000);
        String value = cache.get("test key");
        logger.info("get: {}, {}", "test key", value);
    }


}