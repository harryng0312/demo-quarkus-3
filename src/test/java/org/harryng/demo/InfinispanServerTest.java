package org.harryng.demo;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.RemoteCache;
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


}