package com.hualala.core.api;

import com.hualala.core.base.ResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;

public class BaseMessageConverter extends AbstractHttpMessageConverter<ResultInfo> {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private Logger logger = LoggerFactory.getLogger(BaseMessageConverter.class);

	public BaseMessageConverter() {
		super(new MediaType("text", "html", DEFAULT_CHARSET), new MediaType("application", "json", DEFAULT_CHARSET));
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return true;
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return true;
	}


	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}


	@Override
	protected ResultInfo readInternal(Class<? extends ResultInfo> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		String requestData = StreamUtils.copyToString(inputMessage.getBody(), DEFAULT_CHARSET);
		logger.info("request data [" + requestData + "]");
		System.out.print(requestData);
		return null;
	}

	@Override
	protected void writeInternal(ResultInfo resultInfo, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		HttpServletRequest httpRequest = getHttpRequest();
		long startTime = System.currentTimeMillis();
		String jsondata = resultInfo.toJson();
		if (logger.isInfoEnabled()) {
			logger.info("trans json cost [" + (System.currentTimeMillis() - startTime) + "]ms");
		}
		outputMessage.getHeaders().setContentType(new MediaType("application", "json", DEFAULT_CHARSET));
		StreamUtils.copy(jsondata, DEFAULT_CHARSET, outputMessage.getBody());
	}
	private HttpServletRequest getHttpRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

}
