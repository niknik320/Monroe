package org.moment.monroe.transform;

import org.moment.monroe.Location;

import java.util.ArrayList;
import java.util.stream.Stream;


public class MonroeTransformation{
    public static Stream<Location> transform(String input) {
//        Pattern pattern = Pattern.compile(",");
//        return stream(input.split("\n")).skip(1).map(line -> {
//            String[] x = pattern.split(line);
//            return new Location(x[0], x[1]);
//        });
        Location location = new Location(input.split(",")[0], input.split(",")[1]);
        ArrayList<Location> locations = new ArrayList<>();
        locations.add(location);
        return locations.stream();
    }

    public static Stream<Location> validate(Stream<Location> transformed){
        return transformed.filter(Location::isValid);
    }
}

