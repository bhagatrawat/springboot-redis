package com.bhagat.redis.test.util;

import org.junit.rules.ExternalResource;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * JUnit rule implementation to start and shut down an embedded Redis instance.
 */
public class EmbeddedRedisServer extends ExternalResource {

    private static final int DEFAULT_PORT = 6379;
    private RedisServer server;
    private int port = DEFAULT_PORT;
    private boolean suppressExceptions = false;

    public EmbeddedRedisServer() {
    }

    protected EmbeddedRedisServer(int port) {
        this.port = port;
    }

    public static com.bhagat.redis.test.util.EmbeddedRedisServer runningAt(Integer port) {
        return new com.bhagat.redis.test.util.EmbeddedRedisServer(port != null ? port : DEFAULT_PORT);
    }

    public com.bhagat.redis.test.util.EmbeddedRedisServer suppressExceptions() {
        this.suppressExceptions = true;
        return this;
    }

    @Override
    protected void before() throws IOException {
        try {
            this.server = new RedisServer(this.port);
            this.server.start();
        } catch (Exception e) {
            if (!suppressExceptions) {
                throw e;
            }
        }
    }

    @Override
    protected void after() {
        try {
            this.server.stop();
        } catch (Exception e) {
            if (!suppressExceptions) {
                throw e;
            }
        }
    }
}
