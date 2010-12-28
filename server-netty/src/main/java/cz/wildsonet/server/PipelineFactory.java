package cz.wildsonet.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

public class PipelineFactory implements ChannelPipelineFactory {

    private static ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 0, 0));

    private RackProxy rack;

    public PipelineFactory(RackProxy rack) {
        this.rack = rack;
    }

    public ChannelPipeline getPipeline() throws Exception {

        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());

        pipeline.addLast("executor", executionHandler);

        pipeline.addLast("handler", new Handler(this.rack));

        return pipeline;

    }

}
