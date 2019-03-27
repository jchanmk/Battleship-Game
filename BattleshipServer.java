import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class BattleshipServer {
    public static void main(String[] args) throws IOException {
        ArrayList<Socket> clients = new ArrayList<>();
        ArrayList<Integer> player1Ships = new ArrayList<>();
        ArrayList<Integer> player2Ships = new ArrayList<>();
        ArrayList<Integer> player1Bombs = new ArrayList<>();
        ArrayList<Integer> player2Bombs = new ArrayList<>();
        int p1hits = 0;
        int p2hits = 0;


        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        try (
                ServerSocket serverSocket =
                        new ServerSocket(Integer.parseInt(args[0]));
        ) {
            while(true){
                Socket clientSocket = serverSocket.accept();
                clients.add(clientSocket);
                Client client = new Client(clientSocket, clients, player1Ships, player2Ships, player1Bombs, player2Bombs, p1hits, p2hits);
                client.start();

                for(int i = 0; i < clients.size(); i++){        // This checks to see how many players are in the game
                    Socket curSocket = clients.get(i);
                    PrintWriter out =
                            new PrintWriter(curSocket.getOutputStream(), true);

                    if(clients.size() == 2 ){
                        out.println("-------------------------------------------------------");
                        out.println("               The game can now begin!!");
                        out.println("-------------------------------------------------------");
                        out.println("** Please input coordinates for your 3 battleships **");
                        out.println("** Each battleship must be placed either vertically or horizontally **");
                        out.println("** Ship sizes = 2, 3, 4 **");
                        out.println("** Map size = 10 x 10 **");
                        out.println("** Each pair of coordinates will be written as follows: **");
                        out.println("** (2, 3) (4, 3) will be written as \"2,3 4,3\" **");
                        out.println("** for a 2 unit battle ship placed vertically **");
                        out.println("** Type in the coordinate as shown, no spaces after **");
                        out.println("1) Place your first ship now (size = 2): ");
                    }
                    else{
                        out.println("Waiting for an opponent to join...");
                    }
                }

            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}

class Client extends Thread {
    Socket socket;
    ArrayList<Socket> clients;
    ArrayList<Integer> player1Ships;
    ArrayList<Integer> player2Ships;
    ArrayList<Integer> player1Bombs;
    ArrayList<Integer> player2Bombs;
    public static boolean gameStarted;
    int p1hits;
    int p2hits;
    boolean p1Ship1Alive = true;
    boolean p1Ship2Alive = true;
    boolean p1Ship3Alive = true;
    boolean p2Ship1Alive = true;
    boolean p2Ship2Alive = true;
    boolean p2Ship3Alive = true;


    Client(Socket socket, ArrayList<Socket> clients, ArrayList<Integer> player1Ships,  ArrayList<Integer> player2Ships,  ArrayList<Integer> player1Bombs,
            ArrayList<Integer> player2Bombs, int p1hits, int p2hits){
        this.socket = socket;
        this.clients = clients;
        this.player1Ships = player1Ships;
        this.player2Ships = player2Ships;
        this.player1Bombs = player1Bombs;
        this.player2Bombs = player2Bombs;
        this.p1hits = p1hits;
        this.p2hits = p2hits;
    }

    public void run(){
        try{

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String inputLine;
            int inputPoint;


            while ((inputLine = in.readLine()) != null) {
                Socket player1 = clients.get(0);
                PrintWriter outPlayer1 =
                        new PrintWriter(player1.getOutputStream(), true);

                Socket player2 = clients.get(1);
                PrintWriter outPlayer2 =
                        new PrintWriter(player2.getOutputStream(), true);


                // This records all the input data for bombs
                if(gameStarted){
                    for (int i = 0; i < inputLine.length(); i += 2) {
                        if (i == inputLine.length() - 1) {
                            try{
                                inputPoint = Integer.parseInt(String.valueOf(inputLine.charAt(i)));
                                if(clients.get(0) == socket) {
                                    player1Bombs.add(inputPoint);
                                }else {
                                    player2Bombs.add(inputPoint);
                                }
                            }catch (NumberFormatException e) { }

                        } else if ((inputLine.charAt(i + 1) == ',' || inputLine.charAt(i + 1) == ' ')) {
                            try{
                                inputPoint = Integer.parseInt(String.valueOf(inputLine.charAt(i)));
                                if(clients.get(0) == socket) {
                                    player1Bombs.add(inputPoint);
                                }else {
                                    player2Bombs.add(inputPoint);
                                }
                            }catch (NumberFormatException e) { }
                        }
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////////

                // This loop records the coordinates according to the player's input
                if((player1Ships.size() != 18 || player2Ships.size() != 18) && !inputLine.equals("begin")){
                    ArrayList<Integer>tempShipCoordinatesP1 = new ArrayList<>();
                    ArrayList<Integer>tempShipCoordinatesP2 = new ArrayList<>();

                    for(int j = 0; j < inputLine.length(); j+= 2) {
                        if (j == inputLine.length() - 1) {
                            inputPoint = Integer.parseInt(String.valueOf(inputLine.charAt(j)));
                            if(clients.get(0) == socket) {
                                tempShipCoordinatesP1.add(inputPoint);
                                player1Ships.add(inputPoint);
                            }else {
                                tempShipCoordinatesP2.add(inputPoint);
                                player2Ships.add(inputPoint);
                            }
                        }
                        else if ((inputLine.charAt(j + 1) == ',' || inputLine.charAt(j + 1) == ' ')) {
                            inputPoint = Integer.parseInt(String.valueOf(inputLine.charAt(j)));
                            if(clients.get(0) == socket) {
                                tempShipCoordinatesP1.add(inputPoint);
                                player1Ships.add(inputPoint);
                            }else{
                                tempShipCoordinatesP2.add(inputPoint);
                                player2Ships.add(inputPoint);
                            }
                        }
                    }

                    // This section fills in the gap between ship coordinates
                    if(tempShipCoordinatesP1.size() != 0){
                        if(tempShipCoordinatesP1.get(1).equals(tempShipCoordinatesP1.get(3))){
                            if(tempShipCoordinatesP1.get(0) > tempShipCoordinatesP1.get(2)){
                                for(int i = tempShipCoordinatesP1.get(0)-1; i > tempShipCoordinatesP1.get(2); i--){
                                    player1Ships.add(i);
                                    player1Ships.add(tempShipCoordinatesP1.get(1));
                                    tempShipCoordinatesP1.add(i);
                                    tempShipCoordinatesP1.add(tempShipCoordinatesP1.get(1));
                                }
                            }
                            for(int i = tempShipCoordinatesP1.get(0)+1; i < tempShipCoordinatesP1.get(2); i++){
                                player1Ships.add(i);
                                player1Ships.add(tempShipCoordinatesP1.get(1));
                                tempShipCoordinatesP1.add(i);
                                tempShipCoordinatesP1.add(tempShipCoordinatesP1.get(1));
                            }
                        }
                        else if(tempShipCoordinatesP1.get(0).equals(tempShipCoordinatesP1.get(2))){

                            if(tempShipCoordinatesP1.get(1) > tempShipCoordinatesP1.get(3)){
                                for(int i = tempShipCoordinatesP1.get(1)-1; i > tempShipCoordinatesP1.get(3); i--){
                                    player1Ships.add(tempShipCoordinatesP1.get(2));
                                    player1Ships.add(i);
                                    tempShipCoordinatesP1.add(tempShipCoordinatesP1.get(2));
                                    tempShipCoordinatesP1.add(i);
                                }
                            }
                            else{
                                for(int i = tempShipCoordinatesP1.get(1)+1; i < tempShipCoordinatesP1.get(3); i++){
                                    player1Ships.add(tempShipCoordinatesP1.get(2));
                                    player1Ships.add(i);
                                    tempShipCoordinatesP1.add(tempShipCoordinatesP1.get(2));
                                    tempShipCoordinatesP1.add(i);
                                }
                            }
                        }
                    }

                    if(tempShipCoordinatesP2.size() != 0){
                        if(tempShipCoordinatesP2.get(1).equals(tempShipCoordinatesP2.get(3))){
                            if(tempShipCoordinatesP2.get(0) > tempShipCoordinatesP2.get(2)){
                                for(int i = tempShipCoordinatesP2.get(0)-1; i > tempShipCoordinatesP2.get(2); i--){
                                    player2Ships.add(i);
                                    player2Ships.add(tempShipCoordinatesP2.get(1));
                                    tempShipCoordinatesP2.add(i);
                                    tempShipCoordinatesP2.add(tempShipCoordinatesP2.get(1));
                                }
                            }
                            for(int i = tempShipCoordinatesP2.get(0)+1; i < tempShipCoordinatesP2.get(2); i++){
                                player2Ships.add(i);
                                player2Ships.add(tempShipCoordinatesP2.get(1));
                                tempShipCoordinatesP2.add(i);
                                tempShipCoordinatesP2.add(tempShipCoordinatesP2.get(1));
                            }
                        }
                        else if(tempShipCoordinatesP2.get(0).equals(tempShipCoordinatesP2.get(2))){

                            if(tempShipCoordinatesP2.get(1) > tempShipCoordinatesP2.get(3)){
                                for(int i = tempShipCoordinatesP2.get(1)-1; i > tempShipCoordinatesP2.get(3); i--){
                                    player2Ships.add(tempShipCoordinatesP2.get(2));
                                    player2Ships.add(i);
                                    tempShipCoordinatesP2.add(tempShipCoordinatesP2.get(2));
                                    tempShipCoordinatesP2.add(i);
                                }
                            }
                            else{
                                for(int i = tempShipCoordinatesP2.get(1)+1; i < tempShipCoordinatesP2.get(3); i++){
                                    player2Ships.add(tempShipCoordinatesP2.get(2));
                                    player2Ships.add(i);
                                    tempShipCoordinatesP2.add(tempShipCoordinatesP2.get(2));
                                    tempShipCoordinatesP2.add(i);
                                }
                            }
                        }
                    }
                    ////////////////////////////////////////////////////////////////////////////////////////
                    // Check to see if the ships overlap
                    if(tempShipCoordinatesP1.size() == 6){      // for second ship
                        boolean overlap = false;
                        for (int i = 0; i < tempShipCoordinatesP1.size(); i+=2){
                            for(int j = 0; j < player1Ships.size()-tempShipCoordinatesP1.size(); j+=2){
                                if(tempShipCoordinatesP1.get(i).equals(player1Ships.get(j)) && tempShipCoordinatesP1.get(i+1).equals(player1Ships.get(j+1))){
                                    overlap = true;
                                }
                            }
                        }
                        if(overlap){
                            int sizeForNow = player1Ships.size();
                            for (int i = 4; i < sizeForNow; i++){
                                player1Ships.remove(4);
                            }
                            outPlayer1.println("overlap");
                        }
                        else{
                            outPlayer1.println("noOverlap");
                            outPlayer1.println("3) Place your third ship now (size = 4): ");
                        }
                    }

                    if(tempShipCoordinatesP1.size() == 8){      // for third ship
                        boolean overlap = false;
                        for (int i = 0; i < tempShipCoordinatesP1.size(); i+=2){
                            for(int j = 0; j < player1Ships.size()-tempShipCoordinatesP1.size(); j+=2){
                                if(tempShipCoordinatesP1.get(i).equals(player1Ships.get(j)) && tempShipCoordinatesP1.get(i+1).equals(player1Ships.get(j+1))){
                                    overlap = true;
                                }
                            }
                        }
                        if(overlap){
                            int sizeForNow = player1Ships.size();
                            for (int i = 10; i < sizeForNow; i++){
                                player1Ships.remove(10);
                            }
                            outPlayer1.println("overlap");
                        }
                        else{
                            outPlayer1.println("noOverlap");
                            outPlayer1.println("Successfully placed your ships! Awaiting opponent's submission...");
                        }
                    }


                    if(tempShipCoordinatesP2.size() == 6){      // for second ship player 2
                        boolean overlap = false;
                        for (int i = 0; i < tempShipCoordinatesP2.size(); i+=2){
                            for(int j = 0; j < player2Ships.size()-tempShipCoordinatesP2.size(); j+=2){
                                if(tempShipCoordinatesP2.get(i).equals(player2Ships.get(j)) && tempShipCoordinatesP2.get(i+1).equals(player2Ships.get(j+1))){
                                    overlap = true;
                                }
                            }
                        }
                        if(overlap){
                            int sizeForNow = player2Ships.size();
                            for (int i = 4; i < sizeForNow; i++){
                                player2Ships.remove(4);
                            }
                            outPlayer2.println("overlap");
                        }
                        else{
                            outPlayer2.println("noOverlap");
                            outPlayer2.println("3) Place your third ship now (size = 4): ");
                        }
                    }

                    if(tempShipCoordinatesP2.size() == 8){      // for third ship player 2
                        boolean overlap = false;
                        for (int i = 0; i < tempShipCoordinatesP2.size(); i+=2){
                            for(int j = 0; j < player2Ships.size()-tempShipCoordinatesP2.size(); j+=2){
                                if(tempShipCoordinatesP2.get(i).equals(player2Ships.get(j)) && tempShipCoordinatesP2.get(i+1).equals(player2Ships.get(j+1))){
                                    overlap = true;
                                }
                            }
                        }
                        if(overlap){
                            int sizeForNow = player2Ships.size();
                            for (int i = 10; i < sizeForNow; i++){
                                player2Ships.remove(10);
                            }
                            outPlayer2.println("overlap");
                        }
                        else{
                            outPlayer2.println("noOverlap");
                            outPlayer2.println("Successfully placed your ships! Awaiting opponent's submission...");
                        }
                    }
                }
                ////////////////////////////////////////////////////////////////////////
                if( player1Ships.size() >= 12 && player2Ships.size() >= 12 && inputLine.equals("begin")){
                    outPlayer1.println("Both players successfully placed their ships!");
                    outPlayer2.println("Both players successfully placed their ships!");

                    outPlayer1.println("Your turn first");
                    outPlayer2.println("Opponent's turn...");

                    outPlayer1.println("player1Turn");
                    gameStarted = true;
                }
                ////////////////////////////////////////////////////////////////////////

                // This section is responsible for alternating turns
                if(inputLine.equals("player1Done")){

                    boolean repeatedPoint = false;

                    // This section checks to see if the user already bombed a location
                    for(int i = 0; i < player1Bombs.size()-2; i+=2){
                        if(player1Bombs.size() > 2){
                            if(player1Bombs.get(i).equals(player1Bombs.get(player1Bombs.size()-2))  && player1Bombs.get(i+1).equals( player1Bombs.get(player1Bombs.size()-1))){
                                outPlayer1.println("You already put that bombed that location!");
                                player1Bombs.remove(player1Bombs.size()-2);
                                player1Bombs.remove(player1Bombs.size()-1);
                                repeatedPoint = true;
                            }
                        }
                    }
                    if(!repeatedPoint){

                        // Check to see if hit or miss for player 1's move
                        if(player1Bombs.size() != 0){
                            boolean hit = false;
                            for(int num = 0; num < player2Ships.size(); num+=2){
                                if(player1Bombs.get(player1Bombs.size()-2).equals(player2Ships.get(num))){
                                    if(player1Bombs.get(player1Bombs.size()-1).equals(player2Ships.get(num+1))){
                                        hit = true;
                                        p1hits++;
                                        break;
                                    }
                                }
                            }
                            outPlayer1.println("player1Done");
                            outPlayer2.println("player2Turn");

                            if(hit){
                                outPlayer1.println();
                                outPlayer1.println("Hit!");
                                outPlayer2.println();
                                outPlayer2.println("Opponent Hit!");
                            }
                            else{
                                outPlayer1.println();
                                outPlayer1.println("Miss!");
                                outPlayer2.println();
                                outPlayer2.println("Opponent Missed!");
                            }

                            // check if sunk for first ship
                            int firstShipHitsP1 = 0;
                            int secondShipHitsP1 = 0;
                            int thirdShipHitsP1 = 0;

                            for(int i = 0; i < player1Bombs.size(); i+=2){
                                for (int q = 0; q < 4; q+=2){
                                    if(player1Bombs.get(i).equals(player2Ships.get(q))){
                                        if (player1Bombs.get(i+1).equals(player2Ships.get(q+1))){
                                            firstShipHitsP1++;
                                            break;
                                        }
                                    }
                                }
                            }
                            for(int i = 0; i < player1Bombs.size(); i+=2){
                                for (int q = 4; q < 10; q+=2){
                                    if(player1Bombs.get(i).equals(player2Ships.get(q))){
                                        if (player1Bombs.get(i+1).equals(player2Ships.get(q+1))){
                                            secondShipHitsP1++;
                                            break;
                                        }
                                    }
                                }
                            }
                            for(int i = 0; i < player1Bombs.size(); i+=2){
                                for (int q = 10; q < 18; q+=2){
                                    if(player1Bombs.get(i).equals(player2Ships.get(q))){
                                        if (player1Bombs.get(i+1).equals(player2Ships.get(q+1))){
                                            thirdShipHitsP1++;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(firstShipHitsP1 == 2 && p1Ship1Alive){
                                outPlayer1.println("You sunk their battleship!");
                                outPlayer2.println("They sunk your battleship!");
                                p1Ship1Alive = false;

                            }
                            else if(secondShipHitsP1 == 3 && p1Ship2Alive){
                                outPlayer1.println("You sunk their battleship!");
                                outPlayer2.println("They sunk your battleship!");
                                p1Ship2Alive = false;
                            }
                            else if(thirdShipHitsP1 == 4 && p1Ship3Alive){
                                outPlayer1.println("You sunk their battleship!");
                                outPlayer2.println("They sunk your battleship!");
                                p1Ship3Alive = false;
                            }

                            outPlayer1.println("Your opponents move...");
                            outPlayer2.println("Your Move");
                        }
                    }

                }
                if(inputLine.equals("player2Done")){
                    boolean repeatedPoint2 = false;

                    // This section checks to see if the user already bombed a location
                    for(int i = 0; i < player2Bombs.size()-2; i+=2){
                        if(player2Bombs.size() > 2){
                            if(player2Bombs.get(i).equals(player2Bombs.get(player2Bombs.size()-2))  && player2Bombs.get(i+1).equals( player2Bombs.get(player2Bombs.size()-1))){
                                outPlayer2.println("You already put that bombed that location!");
                                player2Bombs.remove(player2Bombs.size()-2);
                                player2Bombs.remove(player2Bombs.size()-1);
                                repeatedPoint2 = true;
                            }
                        }
                    }
                    if (!repeatedPoint2){
                        // Check to see if hit or miss for player 2's move
                        if(player2Bombs.size() != 0) {
                            boolean hit = false;
                            for (int num = 0; num < player1Ships.size(); num += 2) {
                                if (player2Bombs.get(player2Bombs.size() - 2).equals(player1Ships.get(num))) {
                                    if (player2Bombs.get(player2Bombs.size() - 1).equals(player1Ships.get(num + 1))) {
                                        hit = true;
                                        p2hits++;
                                        break;
                                    }
                                }
                            }
                            outPlayer2.println("player2Done");
                            outPlayer1.println("player1Turn");
                            if(hit){
                                outPlayer2.println();
                                outPlayer2.println("Hit!");
                                outPlayer1.println();
                                outPlayer1.println("Opponent Hit!");
                            }
                            else{
                                outPlayer2.println();
                                outPlayer2.println("Miss!");
                                outPlayer1.println();
                                outPlayer1.println("Opponent Missed!");
                            }
                            // check if sunk for first ship
                            int firstShipHitsP2 = 0;
                            int secondShipHitsP2 = 0;
                            int thirdShipHitsP2 = 0;

                            for(int i = 0; i < player2Bombs.size(); i+=2){
                                for (int q = 0; q < 4; q+=2){
                                    if(player2Bombs.get(i).equals(player1Ships.get(q))){
                                        if (player2Bombs.get(i+1).equals(player1Ships.get(q+1))){
                                            firstShipHitsP2++;
                                            break;
                                        }
                                    }
                                }
                            }
                            for(int i = 0; i < player2Bombs.size(); i+=2){
                                for (int q = 4; q < 10; q+=2){
                                    if(player2Bombs.get(i).equals(player1Ships.get(q))){
                                        if (player2Bombs.get(i+1).equals(player1Ships.get(q+1))){
                                            secondShipHitsP2++;
                                            break;
                                        }
                                    }
                                }
                            }
                            for(int i = 0; i < player2Bombs.size(); i+=2){
                                for (int q = 10; q < 18; q+=2){
                                    if(player2Bombs.get(i).equals(player1Ships.get(q))){
                                        if (player2Bombs.get(i+1).equals(player1Ships.get(q+1))){
                                            thirdShipHitsP2++;
                                            break;
                                        }
                                    }
                                }
                            }

                            if(firstShipHitsP2 == 2 && p2Ship1Alive){
                                outPlayer2.println("You sunk their battleship!");
                                outPlayer1.println("They sunk your battleship!");
                                p2Ship1Alive = false;

                            }

                            else if(secondShipHitsP2 == 3 && p2Ship2Alive){
                                outPlayer2.println("You sunk their battleship!");
                                outPlayer1.println("They sunk your battleship!");
                                p2Ship2Alive = false;
                            }
                            else if(thirdShipHitsP2 == 4 && p2Ship3Alive){
                                outPlayer2.println("You sunk their battleship!");
                                outPlayer1.println("They sunk your battleship!");
                                p2Ship3Alive = false;
                            }

                            outPlayer2.println("Your opponents move...");
                            outPlayer1.println("Your Move");
                        }

                    }

                }
                ////////////////////////////////////////////////////////////////////////


                // This loop figures out if the game is going to start yet, until players put in 12 coordinates
                for(int i = 0; i < clients.size(); i++) {
                    Socket curSocket = clients.get(i);
                    PrintWriter out =
                            new PrintWriter(curSocket.getOutputStream(), true);


                    if (player1Ships.size() == 12 && player2Ships.size() == 12 && inputLine.equals("begin")) {
                        out.println("gameStart");
                    }

                    if (p1hits >= 9){
                        out.println("-------------------- GAME OVER --------------------");
                        out.println("Player 1 Wins!");
                    }
                    else if(p2hits >= 9){
                        out.println("-------------------- GAME OVER --------------------");
                        out.println("Player 2 Wins!");
                    }
                }
            }
        }catch(IOException ie){
                System.out.println("Something went wrong");
        }
    }
}