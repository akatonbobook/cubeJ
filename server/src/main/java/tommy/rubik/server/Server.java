package tommy.rubik.server;

import tommy.rubik.cube.RotateDirection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.RejectedExecutionException;

public class Server {

    public static void main(String[] args) {
        int n;
        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 0 || 65535 < port) {
                System.out.println("ポート番号は0~65535を指定してください");
            }
            if (args.length >= 2)
                n = Integer.parseInt(args[1]);
            else
                n = 3;
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("java -jar CubeServer.jar <ポート番号> <初期分割数>");
            return;
        }
        new Server(port, n);
    }

    private final int MAX_THREAD = 20;
    private final Worker[] threads;
    private final ServerCube cube;

    public Server(int port, int n) {
        cube = new ServerCube(n);
        threads = new Worker[MAX_THREAD];
        try (ServerSocket socket = new ServerSocket(port)) {
            while (true) {
                System.out.println("waiting...");
                Socket s = socket.accept();
                try {
                    for (int i=0; i<MAX_THREAD; i++) {
                        if (threads[i] == null) {
                            threads[i] = new Worker(s, i);
                            threads[i].start();
                            break;
                        }
                    }
                } catch (RejectedExecutionException e) {
                    System.out.println("Rejected execution");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i=0; i<MAX_THREAD; i++) {
                if (threads[i] == null) continue;
                threads[i].interrupt();
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threads[i] = null;
            }
            cube.fireCubeClosed();
        }
    }

    private void executeRotation(String s) {
        char axis = s.charAt(0);
        RotateDirection rd = RotateDirection.fromId(s.charAt(1));
        int layer = Integer.parseInt(s.substring(2));
        switch (axis) {
            case 'X':
                cube.rotateXLayer(layer, rd);
                break;
            case 'Y':
                cube.rotateYLayer(layer, rd);
                break;
            case 'Z':
                cube.rotateZLayer(layer, rd);
                break;
        }
        sendCubeDataAll();
    }

    private void sendAll(String msg) {
        for (int i=0; i<MAX_THREAD; i++) {
            if (threads[i] == null) continue;
            threads[i].send(msg);
        }
    }

    private void sendCubeDataAll() {
        String data = "CUBE " + cube.getN() + " " + cube.toDataString();
        sendAll(data);
    }

    private void sendChat(String chat) {
        sendAll(chat);
    }

    private class Worker extends Thread {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private final int id;

        private Worker(Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }

        private void send(String s) {
            if (out != null)
                out.println(s);
        }

        private void exit() {
            threads[id] = null;
        }

        @Override
        public void run() {
            try {
                socket.setSoTimeout(1000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println("Receive[" + id + "]:\"" + s + "\"");
                    if (s.equals("HELLO")) {
                        socket.setSoTimeout(1000 * 60 * 10);
                        send("CUBE " + cube.getN() + " " + cube.toDataString());
                    } else if (s.startsWith("ROTA ")) {
                        executeRotation(s.substring(5));
                    } else if (s.startsWith("CHAT ")) {
                        sendChat(s);
                    } else if (s.equals("QUIT")) {
                        break;
                    }
                }
                send("BYE");
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
            } finally {
                exit();
            }
        }
    }
}
