import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;

public class Server {
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Servidor escuchando en el puerto " + SERVER_PORT + " ...");
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado.");

            // 1. Generación de par de claves RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Tamaño de clave recomendado
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 2. Envío de la clave pública al cliente
            ObjectOutputStream publicKeyOutputStream = new ObjectOutputStream(socket.getOutputStream());
            publicKeyOutputStream.writeObject(publicKey);
            publicKeyOutputStream.flush();

            // 3. Recepción de la clave AES cifrada y descifrado
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            SealedObject sealedSecretKey = (SealedObject) inputStream.readObject();
            SecretKey secretKey = (SecretKey) sealedSecretKey.getObject(privateKey);

            // 4. Recepción del archivo cifrado
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int encryptedFileSize = dataInputStream.readInt();
            byte[] encryptedFileBytes = new byte[encryptedFileSize];
            dataInputStream.readFully(encryptedFileBytes);

            // 5. Descifrado del archivo
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] fileBytes = aesCipher.doFinal(encryptedFileBytes);

            // 6. Creación del archivo descifrado
            FileOutputStream fileOutputStream = new FileOutputStream("archivo_descifrado.txt");
            fileOutputStream.write(fileBytes);
            fileOutputStream.close();

            // 7. Recepción y comparación del hash SHA-256
            int hashSize = dataInputStream.readInt();
            byte[] clientHash = new byte[hashSize];
            dataInputStream.readFully(clientHash);

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] serverHash = sha256.digest(fileBytes);

            if (MessageDigest.isEqual(clientHash, serverHash)) {
                System.out.println("Archivo transferido correctamente.");
            } else {
                System.out.println("Error: El archivo transferido esta corrupto.");
            }

            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
