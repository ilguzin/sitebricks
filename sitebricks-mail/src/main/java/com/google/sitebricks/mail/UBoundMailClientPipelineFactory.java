package com.google.sitebricks.mail;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * @author <a href="http://youdev.co">Denis Ilguzin</a>
 */
class UBoundMailClientPipelineFactory implements ChannelPipelineFactory {

  public UBoundMailClientPipelineFactory() {
  }

  public ChannelPipeline getPipeline() throws Exception {
    // Create a default pipeline implementation.
    ChannelPipeline pipeline = Channels.pipeline();

    SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
    sslEngine.setUseClientMode(true);
    SslHandler sslHandler = new SslHandler(sslEngine);
    sslHandler.setEnableRenegotiation(true);
    pipeline.addLast("ssl", sslHandler);

    pipeline.addLast("decoder", new StringDecoder());
    pipeline.addLast("encoder", new StringEncoder());

    return pipeline;
  }
}
