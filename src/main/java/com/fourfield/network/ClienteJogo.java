package com.fourfield.network;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Cliente de rede para o modo Multiplayer.
 * Liga-se ao servidor e envia/recebe jogadas.
 */
public class ClienteJogo {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile Consumer<String> onMensagemRecebida;

    /** Cria o cliente com o callback a chamar quando chegar uma mensagem do servidor. */
    public ClienteJogo(Consumer<String> onMensagemRecebida) {
        this.onMensagemRecebida = onMensagemRecebida;
    }

    /** Substitui o callback chamado quando chega uma mensagem, sem afetar a ligação já estabelecida. */
    public void setOnMensagemRecebida(Consumer<String> onMensagemRecebida) {
        this.onMensagemRecebida = onMensagemRecebida;
    }

    /** Liga ao servidor no IP e porta indicados. */
    public void ligar(String ip, int porta) throws IOException {
        socket = new Socket(ip, porta);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Ouve mensagens do servidor em background
        Thread t = new Thread(() -> {
            try {
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

    /** Envia uma mensagem ao servidor. */
    public void enviar(String mensagem) {
        if (out != null) out.println(mensagem);
    }

    /** Fecha a ligação. */
    public void fechar() throws IOException {
        if (socket != null) socket.close();
    }
}
