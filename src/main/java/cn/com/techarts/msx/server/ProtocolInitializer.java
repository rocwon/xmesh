package cn.com.techarts.msx.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;

public class ProtocolInitializer  extends ChannelInitializer<SocketChannel>{
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new HttpServerCodec())
			 .addLast(new HttpObjectAggregator(1024 * 1024))
			 .addLast(new HttpServerExpectContinueHandler())
			 .addLast(new HttpServiceHandler());
		
	}

}
