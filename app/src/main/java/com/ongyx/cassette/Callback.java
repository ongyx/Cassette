package com.ongyx.cassette;

public interface Callback<T> {
    void invoke(T args);
}