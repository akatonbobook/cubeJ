package tommy.rubik.cube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class Server {
    private final int MAX_THREAD = 20;
    private final ExecutorService pool;
    private final ServerCube cube;

    Server(int port, int n) throws IOException {
        cube = new ServerCube(n);
        pool = Executors.newFixedThreadPool(MAX_THREAD);
        try (ServerSocket socket = new ServerSocket(port)) {
            while (true) {
                System.out.println("waiting...");
                Socket s = socket.accept();
                try {
                    pool.execute(new Worker(s));
                } catch (RejectedExecutionException e) {
                    System.out.println("Rejected execution");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

    private void executeRotation(String s) {

    }

    private class Worker implements Runnable {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        private Worker(Socket socket) {
            this.socket = socket;
        }

        private void send(String s) {
            if (out != null)
                out.println(s);
        }

        @Override
        public void run() {
            try {
                socket.setSoTimeout(1000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println("Receive \"" + s + "\"");
                    if (s.equals("HELLO")) {
                        socket.setSoTimeout(1000 * 60 * 10);
                        send("CUBE " + cube.N + " " + cube.toDataString());
                    } else if (s.startsWith("ROTA ")) {
                        executeRotation(s.substring(5));
                    } else if (s.equals("QUIT")) {
                        send("BYE");
                        break;
                    }
                }
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
