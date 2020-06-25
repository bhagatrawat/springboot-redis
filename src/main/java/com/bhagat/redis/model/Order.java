package com.bhagat.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("orders")
@ToString
public class Order implements Serializable {
    @Id
    private Long id;
    @Indexed
    private Date when;
    @Reference
    @ToString.Exclude
    private List<LineItem> lineItems;
}
