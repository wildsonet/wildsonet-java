package cz.wildsonet.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {

    public Server(String host, int port, RackProxy rack){

        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new PipelineFactory(rack));

        bootstrap.bind(new InetSocketAddress(host, port));
    }

}
