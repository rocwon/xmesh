package cn.com.techarts.util;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ClientCachingFilter implements Filter 
{
	private FilterConfig cfg = null;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
	{
		if( res == null || cfg == null) return;
		var response = (HttpServletResponse)res;
		cfg.getInitParameter( "maxAge");
		response.addHeader( "Cache-Control", "public");
		response.addHeader( "Cache-Control", cfg.getInitParameter( "maxAge"));
		chain.doFilter(req, response);
	}

	@Override
	public void init(FilterConfig filterConfig) {
		this.cfg = filterConfig;
	}

	@Override
	public void destroy() {
		this.cfg = null;
	}
}