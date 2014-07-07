package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.Message;
import com.google.sitebricks.mail.imap.MessageStatus;
import com.google.sitebricks.mail.oauth.OAuth2Config;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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

    final String email = "flyflyerson@gmail.com";
    final String password = "ya29.AHES6ZQolsZQH8hxheR8-VnVOVqmEDgGQa6e6RNwrp1YsZC1ka-x3-04cA";
    final String host = "imap.gmail.com";
    final int port = 993;
//    final String host = "localhost";
//    final int port = 9993;

    MailClientHandler.addUserForVerboseLogging(email, true);
    NettyImapClient.addUserForVerboseOutput(email, true);

    nettyImapClientFactory.newNettyImapClient(
      host, port, email, new OAuth2Config(password, "any_text", "any_text"), 5000,
      new MailClient.DisconnectListener() {
        @Override
        public void connectionLost() {
          System.out.println("[" + email + "]: connection lost");
          System.exit(0);
        }

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

                final ListenableFuture<List<MessageStatus>> messages = client.listUidThin(folder, 52283, -1);

                try {
                  for (MessageStatus message : messages.get()) {
                    String sender = "";
                    String senderFrom = message.getSender().get(0);
                    try {
                      InternetAddress internetAddress = new InternetAddress(senderFrom);
                      if (internetAddress.getPersonal() != null)
                        sender = internetAddress.getPersonal();
                      else if (internetAddress.getAddress() != null)
                        sender = internetAddress.getAddress();
                    } catch (AddressException e) {
                      System.out.println("Error parsing sender address. SenderFrom=" + senderFrom);
                    }
                    System.out.println("imapUid=" + message.getImapUid() + "; size=" + message.getSize() +
                      "; subject='" + message.getSubject() + "; senderFrom=" + senderFrom+ "; sender=" + sender);
//                    System.out.println("size=" + message.getSize() +
//                      "; imapUid=" + message.getImapUid() +
//                      "; subject='" + message.getSubject() + "'" +
//                      "; attachmentsNum=" + message.getAttachmentsNum() +
//                      "; bodystructure=" + ((List) message.getBodyStructure().get(0)).get(0));
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
