package com.google.sitebricks.mail;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.ListenableFuture;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.google.sitebricks.mail.imap.Capability;
import com.google.sitebricks.mail.oauth.OAuth2Config;
import com.google.sitebricks.mail.oauth.OAuthConfig;


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
    bootstrap.setOption("connectTimeoutMillis", 5000);

  }

  public void newNettyImapClient(String host, int port, String email,
                                 OAuth2Config oAuth2Config, long timeout,
                                 MailClient.DisconnectListener listener,
                                 NettyImapClient.ClientReadyListener clientReadyListener) {
    MailClientConfig config = new MailClientConfig(host, port, email, oAuth2Config, timeout);
    newNettyImapClient(config, listener, clientReadyListener);
  }

  public void newNettyImapClient(String host, int port, String email,
                                            OAuthConfig oAuthConfig, long timeout,
                                            MailClient.DisconnectListener listener,
                                            NettyImapClient.ClientReadyListener clientReadyListener) {
    MailClientConfig config = new MailClientConfig(host, port, email, oAuthConfig, timeout);
    newNettyImapClient(config, listener, clientReadyListener);
  }

  public void newNettyImapClient(String host, int port, Mail.Auth auth, String email,
                                 String password, long timeout,
                                 MailClient.DisconnectListener listener,
                                 NettyImapClient.ClientReadyListener clientReadyListener) {
    MailClientConfig config = new MailClientConfig(host, port, auth, email, password, timeout);
    newNettyImapClient(config, listener, clientReadyListener);
  }

  private void newNettyImapClient(final MailClientConfig config,
                                  final MailClient.DisconnectListener disconnectListener,
                                  final NettyImapClient.ClientReadyListener clientReadyListener) {

    ChannelFuture future = bootstrap.connect(new InetSocketAddress(config.getHost(), config.getPort()));

    future.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
          final NettyImapClient client = new NettyImapClient(config, bossPool, workerPool, future.getChannel());
          client.init(disconnectListener);

          final ListenableFuture<Capability> capsFuture = client.fetchCapability();

          capsFuture.addListener(new Runnable() {
            @Override
            public void run() {

              if (client.capabilities() != null) {
                final ListenableFuture<String> loginFuture = client.doLogin();

                loginFuture.addListener(new Runnable() {
                  @Override
                  public void run() {
                    if (client.isConnected()) {
                      clientReadyListener.onClientReady(client);
                    }
                    else {
                      clientReadyListener.onClientError(new Exception(client.lastError().message()));
                    }
                  }
                }, workerPool);
              }
              else {
                clientReadyListener.onClientError(new Exception(client.lastError().message()));
              }
            }
          }, workerPool);


        }
        else {
          clientReadyListener.onClientError(new RuntimeException("Could not connect channel", future.getCause()));
          future.getChannel().close();
        }
      }
    });

  }



}
