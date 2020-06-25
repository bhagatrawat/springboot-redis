package com.bhagat.redis;

import com.bhagat.redis.model.LineItem;
import com.bhagat.redis.model.Order;
import com.bhagat.redis.model.Person;
import com.bhagat.redis.repository.LineItemRepository;
import com.bhagat.redis.repository.OrderRepository;
import com.bhagat.redis.service.OrderService;
import lombok.extern.java.Log;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Instant;
import java.util.*;

/**
 *
 */
@Configuration
@Log
public class Beans {
    private final String topic = "chat";

    /**
     * RedisTemplate bean
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    @ConditionalOnProperty(
            value="create.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RedisTemplate<Person, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate template = new RedisTemplate();
        RedisSerializer<Person> values = new Jackson2JsonRedisSerializer<>(Person.class);
        RedisSerializer<String> keys = new StringRedisSerializer();

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(keys);
        template.setValueSerializer(values);
        template.setHashKeySerializer(keys);
        template.setHashValueSerializer(values);
        return template;
    }


    /**
     * Cache Manager Implementation - RedisCacheManager
     */
    @Bean
    @ConditionalOnProperty(
            value="create.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public CacheManager redisCache(RedisConnectionFactory cf) {
        return RedisCacheManager
                .builder(cf)
                .build();
    }

    /**
     * Repository runner - it will insert LineItems and Order in Redis on application startup
     *
     * @param lineItemRepository
     * @param orderRepository
     * @return
     */
    @Bean
    @ConditionalOnProperty(
            value="create.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ApplicationRunner repositories(final LineItemRepository lineItemRepository, final OrderRepository orderRepository) {
        return appRunner("repositories", args -> {
            //Generate Order Id
            Long orderId = generateId();
            List<LineItem> lineItems = Arrays.asList(
                    new LineItem(orderId, generateId(), "iPhone 11"),
                    new LineItem(orderId, generateId(), "MacBook Air"),
                    new LineItem(orderId, generateId(), "iPad")
            );

            lineItems
                    .stream()
                    .map(lineItemRepository::save)
                    .forEach(li -> log.info(li.toString()));

            Order order = new Order(orderId, new Date(), lineItems);
            orderRepository.save(order);
            Collection<Order> orders = orderRepository.findByWhen(order.getWhen());
            orders.forEach(ord -> log.info("Order: " + ord.toString()));
        });
    }

    /**
     * Redis pub/sub instance
     *
     * @param rt
     * @return
     */
    @Bean
    @ConditionalOnProperty(
            value="create.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ApplicationRunner pubSub(final RedisTemplate<String, String> rt) {
        return appRunner("publish/subscribe", args -> {
            rt.convertAndSend(topic, "Hello world @" + Instant.now().toString());
        });
    }

    /**
     * Redis Message Listener Container Bean
     *
     * @param cf
     * @return an instance of RedisMessageListenerContainer
     */
    @Bean
    @ConditionalOnProperty(
            value="create.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RedisMessageListenerContainer fcMessageListener(final RedisConnectionFactory cf) {
        MessageListener ml = (message, pattern) -> {
            String str = new String(message.getBody());
            log.info("Message from '" + topic + "': " + str);
        };
        RedisMessageListenerContainer mlc = new RedisMessageListenerContainer();
        mlc.addMessageListener(ml, new PatternTopic(this.topic));
        mlc.setConnectionFactory(cf);
        return mlc;
    }

    /**
     * Cache Runner
     *
     * @param orderService
     * @return
     */
    @Bean
    @ConditionalOnProperty(
            value="create.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ApplicationRunner cache(final OrderService orderService) {
        return appRunner("caching", a -> {
            Runnable measure = () -> orderService.byId(1L);
            log.info("first: " + measureProcessingTime(measure));
            log.info("two: " + measureProcessingTime(measure));
            log.info("three: " + measureProcessingTime(measure));
        });
    }

    /**
     * @param appName
     * @param rr
     * @return
     */
    private ApplicationRunner appRunner(final String appName, final ApplicationRunner rr) {
        return args -> {
            log.info(appName.toUpperCase() + ":");
            rr.run(args);
        };
    }

    /**
     * @return
     */
    private Long generateId() {
        long tmp = new Random().nextLong();
        return Math.max(tmp, tmp * -1);
    }

    /**
     * @param r
     * @return
     */
    private long measureProcessingTime(final Runnable r) {
        long start = System.currentTimeMillis();
        r.run();
        long end = System.currentTimeMillis();
        return end - start;
    }
}

