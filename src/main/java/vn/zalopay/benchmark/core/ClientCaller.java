package vn.zalopay.benchmark.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import vn.zalopay.benchmark.core.channel.ComponentObserver;
import vn.zalopay.benchmark.core.grpc.ChannelFactory;
import vn.zalopay.benchmark.core.grpc.DynamicGrpcClient;
import vn.zalopay.benchmark.core.message.Reader;
import vn.zalopay.benchmark.core.message.Writer;
import vn.zalopay.benchmark.core.protobuf.ProtoMethodName;
import vn.zalopay.benchmark.core.protobuf.ProtocInvoker;
import vn.zalopay.benchmark.core.protobuf.ServiceResolver;
import vn.zalopay.benchmark.core.specification.GrpcResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientCaller {
    private Descriptors.MethodDescriptor methodDescriptor;
    private JsonFormat.TypeRegistry registry;
    private DynamicGrpcClient dynamicClient;
    private ImmutableList<DynamicMessage> requestMessages;
    private ManagedChannel channel;
    private HostAndPort hostAndPort;
    private Map<String, String> metadataMap;
    private boolean tls;
    private boolean disableTtlVerification;
    ChannelFactory channelFactory;

    public ClientCaller(String HOST_PORT, String TEST_PROTO_FILES, String LIB_FOLDER, String FULL_METHOD, boolean TLS, boolean TLS_DISABLE_VERIFICATION, String METADATA) {
        this.init(HOST_PORT, TEST_PROTO_FILES, LIB_FOLDER, FULL_METHOD, TLS, TLS_DISABLE_VERIFICATION, METADATA);
    }

    private void init(String HOST_PORT, String TEST_PROTO_FILES, String LIB_FOLDER, String FULL_METHOD, boolean TLS, boolean TLS_DISABLE_VERIFICATION, String METADATA) {
        try {
            tls = TLS;
            disableTtlVerification = TLS_DISABLE_VERIFICATION;
            hostAndPort = HostAndPort.fromString(HOST_PORT);
            metadataMap = buildHashMetadata(METADATA);
            channelFactory = ChannelFactory.create();
            ProtoMethodName grpcMethodName =
                    ProtoMethodName.parseFullGrpcMethodName(FULL_METHOD);

            // Fetch the appropriate file descriptors for the service.
            final DescriptorProtos.FileDescriptorSet fileDescriptorSet;

            try {
                fileDescriptorSet = ProtocInvoker.forConfig(TEST_PROTO_FILES, LIB_FOLDER).invoke();
            } catch (Throwable t) {
                shutdownNettyChannel();
                throw new RuntimeException("Unable to resolve service by invoking protoc", t);
            }

            // Set up the dynamic client and make the call.
            ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
            methodDescriptor = serviceResolver.resolveServiceMethod(grpcMethodName);

            createDynamicClient();

            // This collects all known types into a registry for resolution of potential "Any" types.
            registry = JsonFormat.TypeRegistry.newBuilder()
                    .add(serviceResolver.listMessageTypes())
                    .build();
        } catch (Throwable t) {
            shutdownNettyChannel();
            throw t;
        }
    }

    private Map<String, String> buildHashMetadata(String metadata) {
        Map<String, String> metadataHash = new LinkedHashMap<>();

        if (Strings.isNullOrEmpty(metadata))
            return metadataHash;

        String[] keyValue;
        for (String part : metadata.split(",")) {
            keyValue = part.split(":", 2);

            Preconditions.checkArgument(keyValue.length == 2,
                    "Metadata entry must be defined in key1:value1,key2:value2 format: " + metadata);

            metadataHash.put(keyValue[0], keyValue[1]);
        }

        return metadataHash;
    }

    public void createDynamicClient() {
        channel = channelFactory.createChannel(hostAndPort, tls, disableTtlVerification, metadataMap);
        dynamicClient = DynamicGrpcClient.create(methodDescriptor, channel);
    }

    public boolean isShutdown() {
        return channel.isShutdown();
    }

    public boolean isTerminated() {
        return channel.isTerminated();
    }

    public String buildRequest(String jsonData) {
            return buildRequest(jsonData, ImmutableMap.of());
    }

    public String buildRequest(String jsonData, Map<String, BytesFieldContents> bytesFields) {
        try {
            requestMessages = Reader.create(methodDescriptor.getInputType(), jsonData, registry).read();
            requestMessages = setBytesFields(requestMessages, bytesFields);
            return JsonFormat.printer().includingDefaultValueFields().print(requestMessages.get(0));
        } catch (Exception e) {
            shutdownNettyChannel();
            throw new RuntimeException("Caught exception while parsing request for rpc", e);
        }
    }

    public GrpcResponse call(String deadlineMs) {
        long deadline = parsingDeadlineTime(deadlineMs);
        GrpcResponse output = new GrpcResponse();
        StreamObserver<DynamicMessage> streamObserver = ComponentObserver.of(Writer.create(output, registry));
        try {
            dynamicClient.blockingUnaryCall(requestMessages, streamObserver, callOptions(deadline)).get();
        } catch (Throwable t) {
            shutdownNettyChannel();
            throw new RuntimeException("Caught exception while waiting for rpc", t);
        }
        return output;
    }

    public GrpcResponse callServerStreaming(String deadlineMs) {
        long deadline = parsingDeadlineTime(deadlineMs);
        GrpcResponse output = new GrpcResponse();
        StreamObserver<DynamicMessage> streamObserver = ComponentObserver.of(Writer.create(output, registry));
        try {
            dynamicClient.callServerStreaming(requestMessages, streamObserver, callOptions(deadline)).get();
        } catch (Throwable t) {
            shutdownNettyChannel();
            throw new RuntimeException("Caught exception while waiting for rpc", t);
        }
        return output;
    }

    public GrpcResponse callClientStreaming(String deadlineMs) {
        long deadline = parsingDeadlineTime(deadlineMs);
        GrpcResponse output = new GrpcResponse();
        StreamObserver<DynamicMessage> streamObserver = ComponentObserver.of(Writer.create(output, registry));
        try {
            dynamicClient.callClientStreaming(requestMessages, streamObserver, callOptions(deadline)).get();
        } catch (Throwable t) {
            shutdownNettyChannel();
            throw new RuntimeException("Caught exception while waiting for rpc", t);
        }
        return output;
    }

    public GrpcResponse callBidiStreaming(String deadlineMs) {
        long deadline = parsingDeadlineTime(deadlineMs);
        GrpcResponse output = new GrpcResponse();
        StreamObserver<DynamicMessage> streamObserver = ComponentObserver.of(Writer.create(output, registry));
        try {
            dynamicClient.callBidiStreaming(requestMessages, streamObserver, callOptions(deadline)).get();
        } catch (Throwable t) {
            shutdownNettyChannel();
            throw new RuntimeException("Caught exception while waiting for rpc", t);
        }
        return output;
    }

    private static CallOptions callOptions(long deadlineMs) {
        CallOptions result = CallOptions.DEFAULT;
        if (deadlineMs > 0) {
            result = result.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    public void shutdownNettyChannel() {
        try {
            if (channel != null) {
                channel.shutdown();
                channel.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Caught exception while shutting down channel", e);
        }
    }

    private Long parsingDeadlineTime(String deadlineMs) {
        try {
            return Long.parseLong(deadlineMs);
        } catch (Exception e) {
            throw new RuntimeException("Caught exception while parsing deadline to long", e);
        }
    }

    private static ImmutableList<DynamicMessage> setBytesFields(ImmutableList<DynamicMessage> requestMessages, Map<String, BytesFieldContents> bytesFields) {
        DynamicMessage.Builder builder = requestMessages.get(0).toBuilder();
        for (Map.Entry<String, BytesFieldContents> entry : bytesFields.entrySet()) {
                String field = entry.getKey();
                BytesFieldContents contents = entry.getValue();
                String[] pathParts = field.split(".");
                Message.Builder b = builder;
                FieldDescriptor fd = b.getDescriptorForType().findFieldByName(pathParts[0]);
                for (int i = 1; i < pathParts.length; i++) {
                        b = b.getFieldBuilder(fd);
                        fd = builder.getDescriptorForType().findFieldByName(pathParts[i]);
                }
                if (fd.getType() != FieldDescriptor.Type.BYTES) {
                        throw new RuntimeException("some text here", new IllegalArgumentException("Bytes field is not a bytes field"));
                }
                b.setField(fd, contents.readBytes());
        }
        return ImmutableList.of(builder.build());
    }
}
