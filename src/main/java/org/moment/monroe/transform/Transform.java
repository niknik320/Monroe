package org.moment.monroe.transform;

import org.moment.monroe.Location;

import java.util.stream.Stream;

public interface Transform {
    public Stream<Location> transform(String input);

    public Stream<Location> validate(Stream<Location> transformed);

}
