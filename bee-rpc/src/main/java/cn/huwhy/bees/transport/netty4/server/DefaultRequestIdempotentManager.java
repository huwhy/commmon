package cn.huwhy.bees.transport.netty4.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.huwhy.bees.common.RequestState;
import cn.huwhy.bees.core.extension.SpiMeta;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Request;
import cn.huwhy.bees.transport.RequestIdempotentManager;

/**
 * @author huwhy
 * @data 2016/11/24
 * @Desc
 */
@SpiMeta(name = "default")
public class DefaultRequestIdempotentManager implements RequestIdempotentManager {

    private static ScheduledExecutorService              scheduledExecutor = Executors.newScheduledThreadPool(4);
    private static ConcurrentHashMap<Long, RequestState> map               = new ConcurrentHashMap<>();
    private static HashMap<Long, Element>                results           = new HashMap<>();

    @Override
    public RequestState state(Request request) {
        return map.get(request.getRequestId());
    }

    @Override
    public void doing(Request request) {
        map.put(request.getRequestId(), RequestState.DOING);
    }

    @Override
    public void done(Request request, DefaultResponse result) {
        map.put(request.getRequestId(), RequestState.DONE);
        results.put(request.getRequestId(), new Element(result));
    }

    @Override
    public DefaultResponse getResult(Request request) {
        Element e = results.get(request.getRequestId());
        return e == null ? null : e.getResult();
    }

    @Override
    public void clear(Request request) {
        map.remove(request.getRequestId());
        results.remove(request.getRequestId());
    }

    static {
        scheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                Long now = System.currentTimeMillis();
                for (Map.Entry<Long, Element> entry : results.entrySet()) {
                    Element e = entry.getValue();
                    if (now - e.getTime() >= 15000L) {
                        results.remove(entry.getKey());
                        map.remove(entry.getKey());
                    }
                }
            }
        }, 15L, TimeUnit.MINUTES);
    }

    private class Element {
        private DefaultResponse result;

        private Long time;

        Element(DefaultResponse result) {
            this.result = result;
            this.time = System.currentTimeMillis();
        }

        public DefaultResponse getResult() {
            return result;
        }

        public void setResult(DefaultResponse result) {
            this.result = result;
        }

        public Long getTime() {
            return time;
        }

        public void setTime(Long time) {
            this.time = time;
        }
    }
}
