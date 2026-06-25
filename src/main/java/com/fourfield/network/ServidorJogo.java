package com.fourfield.network;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.function.Consumer;

/**
 * Servidor de rede para o modo Multiplayer.
 * Aguarda a ligação de um cliente e envia/recebe jogadas.
 */
public class ServidorJogo {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile Consumer<String> onMensagemRecebida;
    private volatile Runnable onLigado;

    /** Cria o servidor com o callback a chamar quando chegar uma mensagem do cliente. */
    public ServidorJogo(Consumer<String> onMensagemRecebida) {
        this.onMensagemRecebida = onMensagemRecebida;
    }

    /** Substitui o callback chamado quando chega uma mensagem, sem afetar a ligação já estabelecida. */
    public void setOnMensagemRecebida(Consumer<String> onMensagemRecebida) {
        this.onMensagemRecebida = onMensagemRecebida;
    }

    /** Define o callback a chamar logo que o cliente se ligar, independentemente de jogadas. */
    public void setOnLigado(Runnable onLigado) {
        this.onLigado = onLigado;
    }

    /** Inicia o servidor na porta indicada. */
    public void iniciar(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
        // Aguarda ligação em background
        Thread t = new Thread(() -> {
            try {
                clientSocket = serverSocket.accept();
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                if (onLigado != null) onLigado.run();
                // Ouve mensagens do cliente
                String msg;
                while ((msg = in.readLine()) != null) {
                    final String m = msg;
                    if (onMensagemRecebida != null) onMensagemRecebida.accept(m);
                }
            } catch (IOException e) {
                // Ligação encerrada
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Envia uma mensagem ao cliente. */
    public void enviar(String mensagem) {
        if (out != null) out.println(mensagem);
    }

    /** Fecha a ligação e o servidor. */
    public void fechar() throws IOException {
        if (clientSocket != null) clientSocket.close();
        if (serverSocket != null) serverSocket.close();
    }

    /**
     * Tenta descobrir o IP deste computador na rede local, para mostrar a quem vai
     * ligar como cliente (sem ter de ir ver no ipconfig/ifconfig).
     * @return o IP local encontrado, ou "127.0.0.1" se não conseguir descobrir nenhum
     */
    public static String obterIpLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;

                Enumeration<InetAddress> enderecos = ni.getInetAddresses();
                while (enderecos.hasMoreElements()) {
                    InetAddress endereco = enderecos.nextElement();
                    if (endereco instanceof Inet4Address) {
                        return endereco.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // Não conseguiu listar as interfaces de rede
        }
        return "127.0.0.1";
    }
}
