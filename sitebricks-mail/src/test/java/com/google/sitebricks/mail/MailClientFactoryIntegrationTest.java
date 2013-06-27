package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.Message;
import com.google.sitebricks.mail.imap.MessageStatus;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="http://youdev.co">Denis Ilguzin</a>
 */
public class MailClientFactoryIntegrationTest {

  static {
    java.util.logging.ConsoleHandler fh = new java.util.logging.ConsoleHandler();
    java.util.logging.Logger.getLogger("").addHandler(fh);
    java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
  }

  public static void main(String... args) throws InterruptedException, ExecutionException {

    ExecutorService threadPullExecutor = Executors.newCachedThreadPool();

    NettyImapClientFactory nettyImapClientFactory =
      new NettyImapClientFactory(threadPullExecutor, threadPullExecutor);

    final String email = "";
    final String password = "";
    final String host = "imap.gmail.com";
    final int port = 993;
//    final String host = "localhost";
//    final int port = 9993;

    MailClientHandler.addUserForVerboseLogging(email, true);
    NettyImapClient.addUserForVerboseOutput(email, true);

    nettyImapClientFactory.newNettyImapClient(
      host, port, Mail.Auth.SSL, email, password, 10000,
      new MailClient.DisconnectListener() {
        @Override
        public void disconnected() {
          System.out.println("[" + email + "]: disconnected");
          System.exit(0);
        }

        public void idled() {
          System.out.println("[" + email + "]: idled");

        }

        public void unidled() {
          System.out.println("[" + email + "]: unidled");
        }
      },
      new NettyImapClient.ClientReadyListener() {
        @Override
        public void onClientReady(final NettyImapClient client) {
          List<String> capabilities = client.capabilities();
          System.out.println("CAPS: " + capabilities);

//          client.disconnectAsync();

          ListenableFuture<Folder> future = client.open("INBOX", true);

          try {
            final Folder folder;
            folder = future.get();
            future.addListener(new Runnable() {
              @Override
              public void run() {

                final ListenableFuture<List<MessageStatus>> messages = client.listUidThin(folder, 68, -1);

                try {
                  for (MessageStatus message : messages.get()) {
                    System.out.println("imapUid=" + message.getImapUid() +
                      "; subject='" + message.getSubject() + "'; size=" + message.getSize());
                  }

                  client.disconnectAsync();
                } catch (InterruptedException e) {
                  e.printStackTrace();
                } catch (ExecutionException e) {
                  e.printStackTrace();
                }
              }
            }, Executors.newSingleThreadExecutor());
          } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }


        }

        @Override
        public void onClientError(Throwable error) {
          System.out.println("error=" + error.getCause());
        }
      }
    );

  }

}
