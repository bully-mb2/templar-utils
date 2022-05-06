package com.templars_server.util.mqtt;

class MBEventCallback<T> {

    private final MBEventListener<T> listener;

    MBEventCallback(MBEventListener<T> listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unchecked")
    void raiseEvent(Object event) {
        listener.onEvent((T) event);
    }

}
