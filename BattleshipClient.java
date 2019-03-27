//import com.sun.security.ntlm.Server;
//import com.sun.security.ntlm.Server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class BattleshipClient {
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
        ArrayList<Integer> shipCoordinates = new ArrayList<>();

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {
            String userInput;
            String serverInput;
            boolean isPlayer1Turn;
            boolean isPlayer2Turn;

            boolean gameStarted = false;
            boolean gameFinished = false;
            boolean shipOverlap = false;
            boolean shipOverlap2 = true;

            ServerListener serverListener = new ServerListener(echoSocket);
            serverListener.start();
            int inputPoint;
            int steps = 1; // we can tell which step the players are on


            while ((userInput = stdIn.readLine()) != null) {
                isPlayer1Turn = ServerListener.player1Turn;
                isPlayer2Turn = ServerListener.player2Turn;
                shipOverlap = ServerListener.overlap;
                gameFinished = ServerListener.gameOver;

                if (steps == 1) {
                    int shipSize = 2;
                    boolean check = isEnteredCorrectly(userInput, shipSize);

                    if (check) {
                        out.println(userInput);
                        steps++;
                        System.out.println("2) Place your second ship now (size = 3): ");
                    } else {
                        System.out.println("Invalid input");
                    }
                } else if (steps == 2) {
                    int shipSize = 3;
                    boolean check = isEnteredCorrectly(userInput, shipSize);

                    if (check) {
                        out.println(userInput);
                        steps++;
                    } else {
                        System.out.println("Invalid input");
                    }
                } else if (steps == 3) {
                    if(!shipOverlap || !shipOverlap2){
                        int shipSize = 4;
                        boolean check = isEnteredCorrectly(userInput, shipSize);

                        if (check) {
                            out.println(userInput);
                            out.println("begin");
                            steps++;
                        } else {
                            System.out.println("Invalid input");
                        }
                    }
                    else{
                        System.out.println("---- Re input coordinates: ");
                        steps--;
                    }
                }

                else if (steps == 4) {
                    if(gameFinished){
                        steps++;
                    }
                    if(!shipOverlap){
//                        out.println("begin");
                        if (isPlayer1Turn) {
                            boolean check2 = isEnteredCorrectlyBomb(userInput);
                            if(check2){
                                out.println(userInput);
                                out.println("player1Done");
                            }else{
                                System.out.println("Invalid input");
                            }
                        }
                        else if (isPlayer2Turn) {
                            boolean check2 = isEnteredCorrectlyBomb(userInput);
                            if(check2){
                                out.println(userInput);
                                out.println("player2Done");

                            }else{
                                System.out.println("Invalid input");
                            }
                        }
                    }

                    else{

                        System.out.println("---- Re input coordinates: ");
                        shipOverlap2 = false;

                        steps--;
                    }
                }

            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }

    }

    public static boolean isEnteredCorrectly(String str, int shipSize) {
        ArrayList<Integer> tempCoordinates = new ArrayList<>();
        if (str.length()!=7) {
            return false;
        }
        try {
            int point;

            // This loop checks to see if the entry is inputted correctly
            for (int i = 0; i < str.length(); i += 2) {
                if (i == str.length() - 1) {
                    point = Integer.parseInt(String.valueOf(str.charAt(i)));
                    tempCoordinates.add(point);
                } else if ((str.charAt(i + 1) == ',' || str.charAt(i + 1) == ' ')) {
                    point = Integer.parseInt(String.valueOf(str.charAt(i)));
                    tempCoordinates.add(point);
                } else {
                    point = Integer.parseInt(String.valueOf(str.substring(i, i + 2)));
                    tempCoordinates.add(point);
                    i++;
                }
            }
            ////////////////////////////////////////////////////////////////////////

            // This section is the final checks for the input format
            if (tempCoordinates.get(3) > 10) { // if last coordinate is within the bounds
                return false;
            }

            if (!(tempCoordinates.get(0).equals(tempCoordinates.get(2))) && !(tempCoordinates.get(1).equals(tempCoordinates.get(3)))) {   // if coordinates are entered incorrectly (diagonal) send error
                return false;
            } else {
                if (tempCoordinates.get(0).equals(tempCoordinates.get(2))) {                    // checking if the coordinates match the specified ship length
                    if (Math.abs(tempCoordinates.get(1) - tempCoordinates.get(3)) != shipSize - 1) {
                        return false;
                    }
                } else {
                    if (Math.abs(tempCoordinates.get(0) - tempCoordinates.get(2)) != shipSize - 1) {
                        return false;
                    }
                }
            }
            ////////////////////////////////////////////////////////////////////////

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean isEnteredCorrectlyBomb(String str) {
        ArrayList<Integer> tempCoordinatesBomb = new ArrayList<>();

        if (str.length() != 3) {
            return false;
        }
        try {
            int point;

            // This loop checks to see if the entry is inputted correctly
            for (int i = 0; i < str.length(); i += 2) {
                if (i == str.length() - 1) {
                    point = Integer.parseInt(String.valueOf(str.charAt(i)));
                    tempCoordinatesBomb.add(point);
                } else if ((str.charAt(i + 1) == ',' || str.charAt(i + 1) == ' ')) {
                    point = Integer.parseInt(String.valueOf(str.charAt(i)));
                    tempCoordinatesBomb.add(point);
                } else {
                    point = Integer.parseInt(String.valueOf(str.substring(i, i + 2)));
                    tempCoordinatesBomb.add(point);
                    i++;
                }
            }
            ////////////////////////////////////////////////////////////////////////
            // This section is the final checks for the input format
            if (tempCoordinatesBomb.get(1) > 10) { // if last coordinate is within the bounds
                return false;
            }
            ////////////////////////////////////////////////////////////////////////

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}

class ServerListener extends Thread {
    public static Semaphore sem = new Semaphore(1);
    Socket socket;
    public static boolean player1Turn;
    public static boolean player2Turn;
    public static boolean overlap;
    public static boolean gameOver = false;


    ServerListener(Socket socket){
        this.socket = socket;
    }
    public void run(){

        try{
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));

            String serverInput;

            System.out.println("------------------ Welcome to Battleship -------------------");

            while((serverInput = in.readLine()) != null) {

                if(serverInput.equals("gameStart")){
                }
                else if(serverInput.equals("player1Turn")){
                    player1Turn = true;
                }
                else if(serverInput.equals("player2Turn")){
                    player2Turn = true;
                }
                else{
                    if(!serverInput.equals("player1Done") && !serverInput.equals("player2Done") && !serverInput.equals("overlap") && !serverInput.equals("noOverlap"))
                        System.out.println(serverInput);
                }
                if(serverInput.equals("player1Done")){
                    player1Turn = false;
                }
                if(serverInput.equals("player2Done")){
                    player2Turn = false;
                }
                if(serverInput.equals("overlap")){
                    System.out.println("Invalid entry, you cannot overlap ships! Press Enter to re input...");
                    overlap = true;
                }
                if(serverInput.equals("noOverlap")){
                    overlap = false;
                }
                if(serverInput.equals("-------------------- GAME OVER --------------------")){
                    gameOver = true;
                }
            }

        }catch (IOException ie){
        }
    }
}

