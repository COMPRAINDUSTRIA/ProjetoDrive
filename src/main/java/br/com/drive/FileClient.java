package br.com.drive;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Conectado ao servidor.");

            // Solicitar login
            System.out.print("Nome de usuário: ");
            String username = scanner.nextLine();
            System.out.print("Senha: ");
            String password = scanner.nextLine();

            out.println(username);
            out.println(password);
            String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);
            if (!response.equals("Login bem-sucedido")) {
                System.out.println("Falha no login");
                return;
            }

            System.out.println("Login bem-sucedido.");

            // Menu de operações
            while (true) {
                System.out.println("\nEscolha uma operação:");
                System.out.println("1 - Listar arquivos");
                System.out.println("2 - Upload de arquivo");
                System.out.println("3 - Download de arquivo");
                System.out.println("4 - Sair");
                System.out.print("Opção: ");
                int option = scanner.nextInt();
                scanner.nextLine();  // Consumir nova linha

                switch (option) {
                    case 1:
                        out.println("LISTAR");
                        System.out.println("Listando arquivos:");
                        String fileList;
                        while (!(fileList = in.readLine()).isEmpty()) {
                            System.out.println(fileList);
                        }
                        break;

                    case 2:
                        System.out.print("Caminho do arquivo para upload: ");
                        String filePath = scanner.nextLine();
                        out.println("UPLOAD " + getFileExtension(filePath));
                        out.println(new File(filePath).getName());
                        uploadFile(socket, filePath);
                        System.out.println("Arquivo enviado: " + filePath);
                        break;

                    case 3:
                        System.out.print("Nome do arquivo para download: ");
                        String fileName = scanner.nextLine();
                        out.println("DOWNLOAD " + fileName);
                        System.out.print("Caminho para salvar o arquivo: ");
                        String savePath = scanner.nextLine();
                        downloadFile(socket, savePath);
                        System.out.println("Arquivo baixado: " + fileName);
                        break;

                    case 4:
                        System.out.println("Saindo...");
                        return;

                    default:
                        System.out.println("Opção inválida.");
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String filePath) {
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
    }

    private static void uploadFile(Socket socket, String filePath) throws IOException {
        File file = new File(filePath);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = socket.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }

    private static void downloadFile(Socket socket, String savePath) throws IOException {
        File file = new File(savePath);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
             InputStream is = socket.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
        }
    }
}
