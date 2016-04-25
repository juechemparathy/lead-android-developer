package com.gaborbiro.marveldemo.provider.api.model;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo") public class Comics {

    public Data data;

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comics comics = (Comics) o;

        if (data != null ? !data.equals(comics.data) : comics.data != null) return false;

        return true;
    }

    @Override public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override public String toString() {
        return data.toString();
    }
}
