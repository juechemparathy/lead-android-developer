package com.gaborbiro.marveldemo.provider.api.model;

import com.gaborbiro.marveldemo.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo") public class Data {

    public List<Comic> results = new ArrayList<>();

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Data data = (Data) o;

        if (results != null ? !results.equals(data.results) : data.results != null)
            return false;

        return true;
    }

    @Override public int hashCode() {
        return results != null ? results.hashCode() : 0;
    }

    @Override public String toString() {
        return ArrayUtils.join(", ", results, new ArrayUtils.Stringifier<Comic>() {
            @Override public String toString(int index, Comic item) {
                return item.title;
            }
        });
    }
}
