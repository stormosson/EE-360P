package pset.five;

import static org.junit.Assert.*;
import org.junit.Test;

import pset.five.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class ServerTester {

    @Test
    public void testGeneralConcurrency() throws FileNotFoundException {
        /* ExecutorService executorService = Executors.newCachedThreadPool(); */

        Scanner scan = new Scanner(new File("assignments/src/pset/five/input/serverinput1.txt"));
        String[] server_addresses = new String[2];
        
        /* Pull server 1 args out of serverinput1.txt */
        scan = new Scanner(new File("assignments/src/pset/five/input/serverinput1.txt"));
        String server1args[] = new String[5];
        for(int i = 0; i < server1args.length; ++i) {
            server1args[i] = scan.next().trim();
        }
        
        /* Pull server 2 args out of serverinput0.txt */
        scan = new Scanner(new File("assignments/src/pset/five/input/serverinput0.txt"));
        String server0args[] = new String[5];
        for(int i = 0; i < server0args.length; ++i) {
            server0args[i] = scan.next().trim();
        }

        /* Pull client 1 args out of clientinput1.txt */
        scan = new Scanner(new File("assignments/src/pset/five/input/clientinput1.txt"));
        String client1args[] = new String[7];
        for(int i = 0; i < client1args.length; ++i) {
            client1args[i] = scan.next().trim();
        }
        
        /* Pull client 2 args out of clientinput0.txt */
        scan = new Scanner(new File("assignments/src/pset/five/input/clientinput0.txt"));
        String client0args[] = new String[7];
        for(int i = 0; i < client0args.length; ++i) {
            client0args[i] = scan.next().trim();
        }

        /* TODO: spawn launchers so they run concurrently. By launchers I really
         * mean clients but we should also examine this launcher thing too */
        /* Initialize servers */
        /* Launcher.main(server1args); */
        /* Launcher.main(server2args); */

        /* Initialize clients */
        /* Client.main(client1args); */
        /* Client.main(client2args); */

        /* executorService.shutdown(); */
    }
}
