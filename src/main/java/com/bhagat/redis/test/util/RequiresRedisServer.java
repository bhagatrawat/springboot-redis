package com.bhagat.redis.test.util;

import org.junit.AssumptionViolatedException;
import org.junit.rules.ExternalResource;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Socket;

public class RequiresRedisServer extends ExternalResource {

    private int timeout = 30;

    private final String host;
    private final int port;

    private RequiresRedisServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Require a Redis instance listening on {@code localhost:6379}.
     *
     * @return
     */
    public static com.bhagat.redis.test.util.RequiresRedisServer onLocalhost() {
        return new com.bhagat.redis.test.util.RequiresRedisServer("localhost", 6379);
    }

    /**
     * Require a Redis instance listening {@code host:port}.
     *
     * @param host
     * @param port
     * @return
     */
    public static com.bhagat.redis.test.util.RequiresRedisServer listeningAt(String host, int port) {
        return new com.bhagat.redis.test.util.RequiresRedisServer(StringUtils.hasText(host) ? host : "127.0.0.1", port);
    }

    @Override
    protected void before() throws Throwable {
        try (Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(host, port), timeout);
        } catch (Exception e) {
            throw new AssumptionViolatedException(String.format("Seems as Redis is not running at %s:%s.", host, port), e);
        }
    }
}