package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.Message;

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

    final String email = "mailninjatest@gmail.com";
    final String password = "This_app2014";
    final String host = "imap.gmail.com";
    final int port = 993;

    MailClientHandler.addUserForVerboseLogging(email, true);
    NettyImapClient.addUserForVerboseOutput(email, true);

    try {
      final MailClient client = nettyImapClientFactory.newNettyImapClient(
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
        }
      );

      List<String> capabilities = client.capabilities();
      System.out.println("CAPS: " + capabilities);

      ListenableFuture<Folder> future = client.open("INBOX", true);
      final Folder folder = future.get();

      final ExecutorService executor = Executors.newCachedThreadPool();
      future.addListener(new Runnable() {
        @Override
        public void run() {

          final ListenableFuture<List<Message>> messages = client.fetchUids(folder, 1, -1);

          try {
            for (Message message : messages.get()) {
              System.out.println("imapUid=" + message.getImapUid() +
                "; subject" + message.getHeaders().get("Subject"));
            }

            client.disconnect();
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        }
      }, executor);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
