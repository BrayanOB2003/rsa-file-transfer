import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.*;

public class Client {
    private static final String SERVER_HOST = "localhost"; // Or the actual IP if different
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Debe proporcionar un nombre de archivo como parametro.");
            return;
        }

        String filename = args[0];

        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Conectado al servidor.");

            // 1. Recibir la clave pública del servidor
            ObjectInputStream publicKeyInputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey serverPublicKey = (PublicKey) publicKeyInputStream.readObject();

            // 2. Generar clave secreta AES de 256 bits
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); 
            SecretKey secretKey = keyGenerator.generateKey();

            // 3. Cifrar la clave secreta AES con la clave pública del servidor
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
            SealedObject sealedSecretKey = new SealedObject(secretKey, rsaCipher);

            // 4. Enviar la clave secreta AES cifrada al servidor
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(sealedSecretKey);
            outputStream.flush();

            // 5. Cifrado del archivo con AES
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            File file = new File(filename);
            byte[] fileBytes = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileBytes);
            fileInputStream.close();

            byte[] encryptedFileBytes = aesCipher.doFinal(fileBytes);

            // 6. Envío del archivo cifrado
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeInt(encryptedFileBytes.length);
            dataOutputStream.write(encryptedFileBytes);
            dataOutputStream.flush();

            // 7. Cálculo y envío del hash SHA-256 del archivo original
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] fileHash = sha256.digest(fileBytes);

            dataOutputStream.writeInt(fileHash.length);
            dataOutputStream.write(fileHash);
            dataOutputStream.flush();
            
            System.out.println("Archivo enviado correctamente.");

            socket.close();
            System.out.println("Conexion cerrada.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}