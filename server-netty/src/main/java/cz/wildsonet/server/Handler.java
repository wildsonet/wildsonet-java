package cz.wildsonet.server;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Handler extends SimpleChannelUpstreamHandler {

    private final StringBuilder buf = new StringBuilder();

    public void messageReceived(ChannelHandlerContext context, MessageEvent event) throws Exception {

        HttpRequest request = (HttpRequest) event.getMessage();

        Map<String, Object> env = new HashMap<String, Object>();

        int[] rack_version = {1, 1};

        String scheme = "http";
        String hostname = HttpHeaders.getHost(request, "localhost");
        String port = "80";
        String queryString = "";
        String pathInfo = "";

        if(request.containsHeader("X-For-Host")){
            hostname = request.getHeader("X-For-Host");
        }

        if(request.containsHeader("X-For-Scheme")){
            scheme = request.getHeader("X-For-Scheme");
        }

        if(hostname.contains(":")){
            String temp[] = hostname.split(":");
            hostname = temp[0];
            port = temp[1];
        }

        if(request.getUri().contains("?")){
            String[] temp = request.getUri().split("\\?", 2);
            pathInfo = temp[0];
            queryString = temp[1];
        }

        request.removeHeader("Host");
        if((!port.equals("80") && !scheme.equals("http")) && (!port.equals("443") && !scheme.equals("https"))){
            request.addHeader("Host", hostname + ":" + port);
        }else{
            request.addHeader("Host", hostname);
        }

        File tempFile = File.createTempFile(request.getMethod().getName() + ".", ".request", new File("tmp"));
        FileOutputStream uploadStream = new FileOutputStream(tempFile);
        request.getContent().readBytes(uploadStream, request.getContent().capacity());
        uploadStream.close();

        FileInputStream rackInput = new FileInputStream(tempFile);

        env.put("REQUEST_METHOD", request.getMethod().getName());
        env.put("SCRIPT_NAME", "");
        env.put("PATH_INFO", pathInfo);
        env.put("QUERY_STRING", queryString);
        env.put("SERVER_NAME", hostname);
        env.put("SERVER_PORT", port);

        env.put("rack.version", rack_version);
        env.put("rack.url_scheme", scheme);
        env.put("rack.input", rackInput);
        env.put("rack.errors", System.err);
        env.put("rack.multithread", true);
        env.put("rack.multiprocess", false);
        env.put("rack.run_once", false);

        env.put("wsn.request", request);
        env.put("wsn.context", context);

        for (String header : request.getHeaderNames()) {
            env.put("HTTP_" + header.replaceAll("-", "_").toUpperCase(), request.getHeader(header));
        }

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
        response.setHeader("CONTENT_TYPE", "text/plain; charset=UTF-8");

        ChannelFuture future = event.getChannel().write(response);
        future.addListener(ChannelFutureListener.CLOSE);

        rackInput.close();
        tempFile.delete();
    }

}