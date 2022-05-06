package com.templars_server.util.settings;

import java.util.*;
import java.util.stream.Collectors;

class Properties extends java.util.Properties {

    @Override
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        return Collections.synchronizedSet(
                super.entrySet()
                        .stream()
                        .sorted(Comparator.comparing(e -> e.getKey().toString()))
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

}
