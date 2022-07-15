package org.springframework.cloud.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * @author icodening
 * @date 2022.07.12
 */
@ConfigurationProperties(prefix = "grpc")
public class GrpcProperties {

    private Client client = new Client();

    private Server server = new Server();

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public static class Client {

        private DataSize maxInboundMessageSize = DataSize.ofBytes(8 * 1024 * 1024);

        private boolean usePlainText = true;

        public DataSize getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public Client setMaxInboundMessageSize(DataSize maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
            return this;
        }

        public boolean isUsePlainText() {
            return usePlainText;
        }

        public Client setUsePlainText(boolean usePlainText) {
            this.usePlainText = usePlainText;
            return this;
        }
    }

    public static class Server {

        private int port = 30880;

        private DataSize maxInboundMessageSize = DataSize.ofBytes(8 * 1024 * 1024);

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public DataSize getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public void setMaxInboundMessageSize(DataSize maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }
    }
}
