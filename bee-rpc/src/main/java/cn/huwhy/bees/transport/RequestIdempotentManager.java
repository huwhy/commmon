package cn.huwhy.bees.transport;

import cn.huwhy.bees.common.RequestState;
import cn.huwhy.bees.core.extension.Spi;
import cn.huwhy.bees.rpc.DefaultResponse;
import cn.huwhy.bees.rpc.Request;

/**
 * @author huwhy
 * @data 2016/11/24
 * @Desc
 */
@Spi
public interface RequestIdempotentManager {

    RequestState state(Request request);

    void doing(Request request);

    void done(Request request, DefaultResponse result);

    DefaultResponse getResult(Request request);

    void clear(Request request);
}
