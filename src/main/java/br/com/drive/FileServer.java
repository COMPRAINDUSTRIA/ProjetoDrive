package br.com.drive;

import java.io.*;
import java.net.*;
import java.util.*;

public class FileServer {
    private static final int PORT = 12345;
    private static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) {
        // Adiciona usuários e senhas
        users.put("user1", "password1");
        users.put("user2", "password2");
        // Mais usuários...

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escutando na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Autenticação de usuário
                String username = in.readLine();
                String password = in.readLine();
                if (!authenticate(username, password)) {
                    out.println("Login inválido");
                    return;
                }
                out.println("Login bem-sucedido");

                // Criação de pastas, se não existirem
                File userDir = new File("armazenamento/" + username);
                if (!userDir.exists()) {
                    userDir.mkdirs();
                    new File(userDir, "pdf").mkdirs();
                    new File(userDir, "jpg").mkdirs();
                    new File(userDir, "txt").mkdirs();
                }

                // Espera comandos do cliente
                String command;
                while ((command = in.readLine()) != null) {
                    if (command.equals("LISTAR")) {
                        // Listar arquivos
                        listFiles(userDir, out);
                    } else if (command.startsWith("DOWNLOAD ")) {
                        // Download de arquivo
                        String fileName = command.substring(9);
                        sendFile(new File(userDir, fileName), socket.getOutputStream());
                    } else if (command.startsWith("UPLOAD ")) {
                        // Upload de arquivo
                        String fileType = command.substring(7);
                        receiveFile(new File(userDir, fileType + "/" + in.readLine()), in);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean authenticate(String username, String password) {
            return users.containsKey(username) && users.get(username).equals(password);
        }

        private void listFiles(File dir, PrintWriter out) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    listFiles(file, out);
                } else {
                    out.println(file.getName());
                }
            }
        }

        private void sendFile(File file, OutputStream os) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void receiveFile(File file, BufferedReader in) {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                char[] buffer = new char[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(new String(buffer, 0, bytesRead).getBytes());
                }
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
