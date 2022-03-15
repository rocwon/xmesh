package cn.com.techarts.msx.server;

import java.util.Map;

import cn.com.techarts.data.SimpleDaoHelper;
import cn.techarts.jhelper.INIReader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server{
	private INIReader configs;
	private SimpleDaoHelper persister = null;
	private EventLoopGroup bossGroup = null, workerGroup = null;
	private static String configPath = "D:\\Studio\\Project\\Java\\xmesh\\config.ini";

	public static void main(String[] args) throws Exception {
		var configs = new INIReader(configPath);
		new Server(configs).start();
	}
	
	public Server(INIReader configs) {
		this.configs = configs;
		if(configs == null) {
			throw new RuntimeException("Missing config");
		}
		var jdbc = configs.getSections().get("jdbc");
		var statements = configs.getSections().get("sql");
		this.initDataAccessUtility(jdbc, statements);
	}
	
	public void start() {
		int epoll = configs.getInt("server", "epoll");
		int webPort = configs.getInt("server", "port");
		startDualProtocolServer(webPort, epoll == 1);
	}
	
	private void startDualProtocolServer(int webPort, boolean epoll) {
		bossGroup = initEventLoopThreadPool(epoll);
		workerGroup = initEventLoopThreadPool(epoll);
		try {
			var bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			initBootstrapSettings(bootstrap, epoll);
			bootstrap.childHandler(new ProtocolInitializer());
			ChannelFuture web = bootstrap.bind(webPort).sync();
			
			web.channel().closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					future.channel().close();
				}
			});

		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to start server.", e);
		}
	}
	
	private void initBootstrapSettings(ServerBootstrap bootstrap, boolean epoll) {
		bootstrap.option(ChannelOption.SO_BACKLOG, 128)
				 .childOption(ChannelOption.TCP_NODELAY,true)
				 .childOption(ChannelOption.SO_KEEPALIVE, true);
		if(epoll) {
			bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
			bootstrap.channel(EpollServerSocketChannel.class);
		}else {
			bootstrap.channel(NioServerSocketChannel.class);
		}
	}
	
	private EventLoopGroup initEventLoopThreadPool(boolean epoll) {
		EventLoopGroup result = null;
		if(epoll) {
			result = new EpollEventLoopGroup();
		}else {
			result = new NioEventLoopGroup();
		}
		return result;
	}

	private void initDataAccessUtility(Map<String, String> jdbc, Map<String, String> statements) {
		var url = jdbc.get("url");
		var user = jdbc.get("user");
		var token = jdbc.get("token");
		var driver = jdbc.get("driver");
		LocalCache.cacheSqlStatements(statements);
		persister = new SimpleDaoHelper(driver, url, user, token);
		LocalCache.initRestfulHandlers(persister); //Cache all handlers
	}
}