package vn.zalopay.benchmark;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.zalopay.benchmark.core.BytesFieldContents;
import vn.zalopay.benchmark.core.ClientCaller;
import vn.zalopay.benchmark.core.specification.GrpcResponse;
import vn.zalopay.benchmark.util.GRPCBytesField;
import vn.zalopay.benchmark.util.GRPCBytesFields;

public class GRPCSampler extends AbstractSampler implements ThreadListener {

    private static final Logger log = LoggerFactory.getLogger(GRPCSampler.class);
    private static final long serialVersionUID = 232L;

    public static final String METADATA = "GRPCSampler.metadata";
    public static final String LIB_FOLDER = "GRPCSampler.libFolder";
    public static final String PROTO_FOLDER = "GRPCSampler.protoFolder";
    public static final String HOST = "GRPCSampler.host";
    public static final String PORT = "GRPCSampler.port";
    public static final String FULL_METHOD = "GRPCSampler.fullMethod";
    public static final String REQUEST_JSON = "GRPCSampler.requestJson";
    public static final String BINARY_FIELDS = "GRPCSampler.binaryFields";
    public static final String DEADLINE = "GRPCSampler.deadline";
    public static final String TLS = "GRPCSampler.tls";
    public static final String TLS_DISABLE_VERIFICATION = "GRPCSampler.tlsDisableVerification";
    private transient ClientCaller clientCaller = null;

    public GRPCSampler() {
        trace("init GRPCSampler");
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle() {
        return this.getName();
    }

    private void trace(String s) {
        String threadName = Thread.currentThread().getName();
        log.debug("{} ({}) {} {} {}", threadName,
                getTitle(), s, this.toString());
    }

    private void initGrpcClient() {
        if (clientCaller == null) {
            clientCaller = new ClientCaller(
                    getHostPort(),
                    getProtoFolder(),
                    getLibFolder(),
                    getFullMethod(),
                    isTls(),
                    isTlsDisableVerification(),
                    getMetadata());
        }
    }

    @Override
    public SampleResult sample(Entry ignored) {
        GrpcResponse grpcResponse = new GrpcResponse();
        SampleResult sampleResult = new SampleResult();
        try {
            initGrpcClient();
            sampleResult.setSampleLabel(getName());
            String grpcRequest = clientCaller.buildRequest(getRequestJson(), getBytesFieldsAsMap());
            sampleResult.setSamplerData(grpcRequest);
            sampleResult.sampleStart();
            grpcResponse = clientCaller.call(getDeadline());
            sampleResult.sampleEnd();
            sampleResult.setSuccessful(true);
            sampleResult.setResponseData(grpcResponse.getGrpcMessageString().getBytes(StandardCharsets.UTF_8));
            sampleResult.setResponseMessage("Success");
            sampleResult.setDataType(SampleResult.TEXT);
            sampleResult.setResponseCodeOK();
        } catch (RuntimeException e) {
            errorResult(grpcResponse, sampleResult, e);
            e.printStackTrace();
        }
        return sampleResult;
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public void threadStarted() {
        log.debug("{}\ttestStarted", whoAmI());
    }

    @Override
    public void threadFinished() {
        log.debug("{}\ttestEnded", whoAmI());
        if (clientCaller != null) {
            clientCaller.shutdownNettyChannel();
            clientCaller = null;
        }
    }

    private String whoAmI() {
        return Thread.currentThread().getName() +
                "@" +
                Integer.toHexString(hashCode()) +
                "-" +
                getName();
    }

    private void errorResult(GrpcResponse grpcResponse, SampleResult sampleResult, Exception e) {
        if(sampleResult.getStartTime() != 0) {
            sampleResult.sampleEnd();
        }
        sampleResult.setSuccessful(false);
        Throwable t = e;
        if (e.getCause() != null) {
            t = e.getCause();
        }
        sampleResult.setResponseData(String.format("Exception: %s. %s", t.getMessage(), grpcResponse.getGrpcMessageString()), "UTF-8");
        sampleResult.setResponseMessage("Exception: " + t.getMessage());
        sampleResult.setDataType(SampleResult.TEXT);
        sampleResult.setResponseCode("500");
    }

    /**
     * GETTER AND SETTER
     */

    public String getMetadata() {
        return getPropertyAsString(METADATA);
    }

    public void setMetadata(String metadata) {
        setProperty(METADATA, metadata);
    }

    public String getLibFolder() {
        return getPropertyAsString(LIB_FOLDER);
    }

    public void setLibFolder(String libFolder) {
        setProperty(LIB_FOLDER, libFolder);
    }

    public String getProtoFolder() {
        return getPropertyAsString(PROTO_FOLDER);
    }

    public void setProtoFolder(String protoFolder) {
        setProperty(PROTO_FOLDER, protoFolder);
    }

    public String getFullMethod() {
        return getPropertyAsString(FULL_METHOD);
    }

    public void setFullMethod(String fullMethod) {
        setProperty(FULL_METHOD, fullMethod);
    }

    public String getRequestJson() {
        return getPropertyAsString(REQUEST_JSON);
    }

    public void setRequestJson(String requestJson) {
        setProperty(REQUEST_JSON, requestJson);
    }

    public GRPCBytesFields getBytesFields() {
        GRPCBytesFields field = (GRPCBytesFields) getProperty(BINARY_FIELDS).getObjectValue();
        if(field == null) {
            return new GRPCBytesFields();
        }
        return field;
    }

    public HashMap<String, BytesFieldContents> getBytesFieldsAsMap() {
        GRPCBytesFields fields = getBytesFields();
        HashMap<String, BytesFieldContents> contents = new HashMap<>();
        CollectionProperty binaryFields = fields.getGRPCBinaryFieldsCollection();
        for (int i = 0; i < binaryFields.size(); i++) {
            GRPCBytesField field = (GRPCBytesField) binaryFields.get(i).getObjectValue();
            contents.put(field.getFieldPath(), new
                            BytesFieldContents(field.getFilePath(),
                                    field.getOffset(), field.getReadLength()));
        }
        return contents;
    }

    public void setBytesFields(GRPCBytesFields binaryFields) {
        if (binaryFields.getGRPCBinaryFieldCount() > 0) {
            setProperty(new TestElementProperty(BINARY_FIELDS, binaryFields));
        } else {
            removeProperty(BINARY_FIELDS); // no point saving an empty list
        }
    }

    public String getDeadline() {
        return getPropertyAsString(DEADLINE);
    }

    public void setDeadline(String deadline) {
        setProperty(DEADLINE, deadline);
    }

    public boolean isTls() {
        return getPropertyAsBoolean(TLS);
    }

    public void setTls(boolean tls) {
        setProperty(TLS, tls);
    }

    public boolean isTlsDisableVerification() {
        return getPropertyAsBoolean(TLS_DISABLE_VERIFICATION);
    }

    public void setTlsDisableVerification(boolean tlsDisableVerification) {
        setProperty(TLS_DISABLE_VERIFICATION, tlsDisableVerification);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setHost(String host) {
        setProperty(HOST, host);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
    }

    public void setPort(String port) {
        setProperty(PORT, port);
    }

    private String getHostPort() {
        return getHost() + ":" + getPort();
    }
}
