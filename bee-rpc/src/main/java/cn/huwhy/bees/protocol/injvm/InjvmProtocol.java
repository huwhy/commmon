package cn.huwhy.bees.protocol.injvm;//package cn.huwhy.bees.protocol.injvm;
//
//import cn.huwhy.bees.core.extension.SpiMeta;
//import cn.huwhy.bees.exception.MotanErrorMsgConstant;
//import cn.huwhy.bees.exception.MotanServiceException;
//import cn.huwhy.bees.rpc.AbstractExporter;
//import cn.huwhy.bees.rpc.AbstractReferer;
//import cn.huwhy.bees.rpc.Exporter;
//import cn.huwhy.bees.rpc.Provider;
//import cn.huwhy.bees.rpc.Referer;
//import cn.huwhy.bees.rpc.Request;
//import cn.huwhy.bees.rpc.Response;
//import cn.huwhy.bees.rpc.URL;
//import cn.huwhy.bees.util.LoggerUtil;
//import cn.huwhy.bees.util.MotanFrameworkUtil;
//
///**
// * JVM 节点内部的调用
// *
// * <pre>
// * 		1) provider 和 referer 相对应
// * 		2) provider 需要在被consumer refer 之前需要 export
// * </pre>
// *
// * @author maijunsheng
// *
// */
//@SpiMeta(name = "injvm")
//public class InjvmProtocol extends AbstractProtocol {
//
//    @Override
//    protected <T> Exporter<T> createExporter(Provider<T> provider, URL url) {
//        return new InJvmExporter<T>(provider, url);
//    }
//
//    @Override
//    protected <T> Referer<T> createReferer(Class<T> clz, URL url, URL serviceUrl) {
//        return new InjvmReferer<T>(clz, url, serviceUrl);
//    }
//
//    /**
//     * injvm provider
//     *
//     * @author maijunsheng
//     *
//     * @param <T>
//     */
//    class InJvmExporter<T> extends AbstractExporter<T> {
//        public InJvmExporter(Provider<T> provider, URL url) {
//            super(provider, url);
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public void unExport() {
//            String protocolKey = MotanFrameworkUtil.getProtocolKey(url);
//
//            Exporter<T> exporter = (Exporter<T>) exporterMap.remove(protocolKey);
//
//            if (exporter != null) {
//                exporter.destroy();
//            }
//
//            LoggerUtil.info("InJvmExporter unExport Success: url=" + url);
//        }
//
//        @Override
//        protected boolean doInit() {
//            return true;
//        }
//
//        @Override
//        public void destroy() {}
//    }
//
//    /**
//     * injvm consumer
//     *
//     * @author maijunsheng
//     *
//     * @param <T>
//     */
//    class InjvmReferer<T> extends AbstractReferer<T> {
//        private Exporter<T> exporter;
//
//        public InjvmReferer(Class<T> clz, URL url, URL serviceUrl) {
//            super(clz, url, serviceUrl);
//        }
//
//        @Override
//        protected Response doCall(Request request) {
//            if (exporter == null) {
//                throw new MotanServiceException("InjvmReferer call Error: provider not exist, url=" + url.getUri(),
//                        MotanErrorMsgConstant.SERVICE_UNFOUND);
//            }
//
//            return exporter.getProvider().call(request);
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        protected boolean doInit() {
//            String protocolKey = MotanFrameworkUtil.getProtocolKey(url);
//
//            exporter = (Exporter<T>) exporterMap.get(protocolKey);
//
//            if (exporter == null) {
//                LoggerUtil.error("InjvmReferer init Error: provider not exist, url=" + url);
//                return false;
//            }
//
//            return true;
//        }
//
//        @Override
//        public void destroy() {}
//    }
//}
