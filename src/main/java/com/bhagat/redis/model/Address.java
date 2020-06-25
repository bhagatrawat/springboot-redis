package com.bhagat.redis.model;

import lombok.Data;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.index.GeoIndexed;
import org.springframework.data.redis.core.index.Indexed;

@Data
public class Address {

    private @Indexed
    String city;
    private String country;
    private @GeoIndexed
    Point location;
}