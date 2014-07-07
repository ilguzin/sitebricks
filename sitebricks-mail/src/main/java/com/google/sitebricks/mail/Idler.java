package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import org.jboss.netty.channel.ChannelFuture;

/**
* @author dhanji@gmail.com (Dhanji R. Prasanna)
*/
interface Idler {
  ChannelFuture done();

  ListenableFuture doneFuture();

  void disconnectAsync();

  void idleEnd();

  void idleStart();
}
