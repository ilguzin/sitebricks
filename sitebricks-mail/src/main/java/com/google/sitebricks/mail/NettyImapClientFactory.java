package com.google.sitebricks.mail;

import com.google.sitebricks.mail.imap.AuthErrorException;
import com.google.sitebricks.mail.oauth.OAuth2Config;
import com.google.sitebricks.mail.oauth.OAuthConfig;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;


/**
 * Factory that is intended to make connect to server, instantiate new channel and
 * make new NettyImapClient use it with its concrete channel Handler.
 *
 * @author <a href="http://youdev.co">Denis Ilguzin</a>
 */
public class NettyImapClientFactory {

  private final ClientBootstrap bootstrap;

  private final ExecutorService bossPool;
  private final ExecutorService workerPool;

  public NettyImapClientFactory(ExecutorService bossPool,
                                ExecutorService workerPool) {

    this.bossPool = bossPool;
    this.workerPool = workerPool;

    bootstrap = new ClientBootstrap(
            new NioClientSocketChannelFactory(this.bossPool, this.workerPool));

    bootstrap.setPipelineFactory(new UBoundMailClientPipelineFactory());

  }

  public NettyImapClient newNettyImapClient(String host, int port, String email, OAuth2Config oAuth2Config, long timeout,
                                            final MailClient.DisconnectListener listener) throws AuthErrorException {
    MailClientConfig config = new MailClientConfig(host, port, email, oAuth2Config, timeout);
    return newNettyImapClient(config, listener);
  }

  public NettyImapClient newNettyImapClient(String host, int port, String email, OAuthConfig oAuthConfig, long timeout,
                                            final MailClient.DisconnectListener listener) throws AuthErrorException {
    MailClientConfig config = new MailClientConfig(host, port, email, oAuthConfig, timeout);
    return newNettyImapClient(config, listener);
  }

  public NettyImapClient newNettyImapClient(String host, int port, Mail.Auth auth, String email,
                                            String password, long timeout,
                                            final MailClient.DisconnectListener listener) throws AuthErrorException {
    MailClientConfig config = new MailClientConfig(host, port, auth, email, password, timeout);
    return newNettyImapClient(config, listener);
  }

  private NettyImapClient newNettyImapClient(final MailClientConfig config,
                                            final MailClient.DisconnectListener listener) throws AuthErrorException {

    ChannelFuture future = bootstrap.connect(
      new InetSocketAddress(config.getHost(), config.getPort()));

    future.awaitUninterruptibly();

    if (future.isSuccess()) {
      NettyImapClient client = new NettyImapClient(config, bossPool, workerPool, future.getChannel());
      if (!client.init(listener)) {
        throw new AuthErrorException("");
      }
      return client;
    }
    else {
      throw new RuntimeException("Could not connect channel", future.getCause());
    }

  }



}
