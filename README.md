# Secure File Transfer with Symmetric Key

This project consists of two programs, a client and a server, that enable secure file transfer using rsa system with AES algorithm with a 256-bit key for file encryption. Additionally, the SHA-256 hash of the transferred file is calculated, and its integrity is verified.

## Prerequisites

- Java Development Kit (JDK) 8 or higher.
- Git (optional) to clone the repository.

## Execution

1. Clone or download the project repository to your local machine.

- ```git clone https://github.com/your-username/rsa-file-transfer.git```

2. Open a terminal and navigate to the project directory.

- ```cd src/java```

3. Compile the Java files.

- ```javac Server.java```

- ```javac Client.java```

4. Run the server program in one terminal.

- ```java Server```

5. In another terminal, run the client program, providing the filename to transfer as a parameter.

- ```java Client file.txt```

Make sure to replace `file.txt` with the actual name of the file you want to transfer.

6. Check the server terminal output to verify if the file was transferred successfully.

- ```File transferred successfully.```
