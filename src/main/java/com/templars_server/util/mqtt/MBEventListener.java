package com.templars_server.util.mqtt;

public interface MBEventListener<T> {

    void onEvent(T event);

}