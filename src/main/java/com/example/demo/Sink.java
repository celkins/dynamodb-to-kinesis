package com.example.demo;

@FunctionalInterface
public interface Sink<T> {

    void accept(T t);
}
