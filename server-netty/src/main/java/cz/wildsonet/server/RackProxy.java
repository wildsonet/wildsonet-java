package cz.wildsonet.server;

import java.util.Map;

public interface RackProxy {
    public void call(Map<String, Object> env);
}
