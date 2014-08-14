package com.github.duffy356.maeh.exchanger;

import java.util.List;
import java.util.Map;

/**
 * Created by doba on 10.04.2014.
 */
public interface ICallback extends ICallbackName {
    public void messageReceived(Map<String, Object> s, Integer senderId);
    public void messageSent(Integer receivers);
}
