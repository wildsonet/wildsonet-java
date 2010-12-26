package cz.wildsonet.server;

public class Main {

    public static void main(String[] args){
        String host = "0.0.0.0";
        int port = 3000;

        if(args.length > 0) host = args[0];
        if(args.length > 1) port = Integer.decode(args[1]);

        new Server(host, port);

    }

}
