package tommy.rubik;

import tommy.rubik.cube.event.CubeEvent;
import tommy.rubik.cube.event.CubeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Client extends JFrame implements CubeListener {
    private final CubePanel cubePanel;
    final JPanel chatPanel;
    final JTextField chatField;
    final JButton chatSendBtn;

    final ArrayList<Chat> chats;

    private boolean isClosing = false;
    public Client(int width, int height) {
        super("Client");
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chats = new ArrayList<>();
        cubePanel = new CubePanel(this);
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatField = new JTextField();
        chatSendBtn = new JButton("送信");
        chatSendBtn.addActionListener(e -> chat());
        getRootPane().setDefaultButton(chatSendBtn);
        chatPanel.add(chatField);
        chatPanel.add(chatSendBtn, BorderLayout.EAST);
        add(cubePanel);
        add(chatPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                isClosing = true;
                if (cubePanel.cube != null)
                    cubePanel.cube.close();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        setVisible(true);
        new ServerSelectWindow();
    }

    public static void main(String[] args) {
        new Client(600, 400);
    }

    class Chat {
        static final int size = 23;
        int x;
        int y;
        final String text;

        Chat(String text, int x) {
            this.text = text;
            this.x = x;
            this.y = 30;
        }

        int animation() {
            x -= 4;
            return x;
        }
    }

    private class ServerSelectWindow extends JDialog {
        private static final int width = 250;
        private static final int height = 250;
        ServerSelectWindow() {
            super(Client.this, true);
            setResizable(false);
            setTitle("サーバ情報を入力してください");
            setLocationRelativeTo(Client.this);
            Rectangle r = Client.this.getBounds();
            setBounds(r.x + r.width/2 - width/2, r.y + r.height/2 - height/2, width, height);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel jp = new JPanel();
            jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));

            JPanel hp = new JPanel(new BorderLayout());
            JLabel hl = new JLabel("HOST : ");
            JTextField hf = new JTextField();
            hp.add(hl, BorderLayout.WEST);
            hp.add(hf, BorderLayout.CENTER);
            hp.setMaximumSize(new Dimension(width - 20, 30));
            hp.setAlignmentX(0.5f);

            JPanel pp = new JPanel(new BorderLayout());
            JLabel pl = new JLabel("PORT : ");
            JTextField pf = new JTextField();
            pp.add(pl, BorderLayout.WEST);
            pp.add(pf, BorderLayout.CENTER);
            pp.setMaximumSize(new Dimension(width - 20, 30));
            pp.setAlignmentX(0.5f);

            JButton jb = new JButton("接続");
            jb.setMaximumSize(new Dimension(100, 25));
            jb.setAlignmentX(0.5f);
            getRootPane().setDefaultButton(jb);
            jb.addActionListener(e -> {
                try {
                    dispose();
                    createClientCube(hf.getText(), Integer.parseInt(pf.getText()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ServerSelectWindow.this, "サーバ情報が正しくありません");
                    new ServerSelectWindow();
                }
            });

            jp.add(Box.createGlue());
            jp.add(hp);
            jp.add(Box.createRigidArea(new Dimension(1, 30)));
            jp.add(pp);
            jp.add(Box.createRigidArea(new Dimension(1, 30)));
            jp.add(jb);
            jp.add(Box.createGlue());

            add(jp);

            setVisible(true);
        }
    }

    private void chat() {
        String chat = chatField.getText();
        chat = chat.replaceAll("\n", "");
        if (chat.equals("")) return;
        else if (chat.startsWith("/axis")) cubePanel.showAxis = !cubePanel.showAxis;
        else if (chat.startsWith("/collision")) cubePanel.showCollision = !cubePanel.showCollision;
        else send("CHAT " + chat);
        chatField.setText("");
    }

    private Socket socket;
    private ClientThread thread;

    public void createClientCube(String address, int port) {
        try {
            cubePanel.cube = new ClientCube(this);
            cubePanel.cube.addCubeListener(this);
            socket = new Socket(address, port);
            thread = new ClientThread();
            thread.start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "サーバに接続できませんでした");
            new ServerSelectWindow();
        }
    }

    public void send(String msg) {
        System.out.println("S " + msg);
        thread.send(msg);
    }

    public boolean threadIsAlive() {
        return thread.isAlive();
    }

    public void threadJoin() throws InterruptedException {
        thread.join();
    }

    private class ClientThread extends Thread {
        private BufferedReader in;
        private PrintWriter out;

        public void send(String msg) {
            if (out != null)
                out.println(msg);
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                send("HELLO");
                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println("Received: " + s);
                    if (s.startsWith("CUBE ")) {
                        String[] split = s.substring(5).split(" ");
                        System.out.println(Integer.parseInt(split[0]));
                        if (cubePanel.cube.getN() != Integer.parseInt(split[0])) {
                            cubePanel.cube.changeN(Integer.parseInt(split[0]));
                        }
                        cubePanel.cube.applyDataString(split[1]);
                    } else if (s.startsWith("CHAT ")) {
                        chats.add(new Chat(s.substring(5), getSize().width));
                    } else if (s.equals("BYE")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException");
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cubePanel.cube.fireCubeClosed();
        }
    }

    @Override
    public void cubeClosed(CubeEvent e) {
        if (isClosing) return;
        new ServerSelectWindow();
    }

    @Override
    public void cubeCreated(CubeEvent e) {

    }
}
