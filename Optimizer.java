/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Double.max;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ryan
 */
public class Optimizer {

    public static ArrayList<Player> Players = new ArrayList<>();
    public static Entry[][][] table;
    public static int numplayers = 0;
    public static Lineup finalLineup = new Lineup();

    public static void main(String[] args) throws IOException {
        BufferedReader br = null;
        int i, j, k;

        try {
            String sCurrentLine;
            String[] line;
            br = new BufferedReader(new FileReader("C:\\Users\\ryan\\Desktop\\optimizer\\playersTest.txt"));

            while ((sCurrentLine = br.readLine()) != null) {
                line = sCurrentLine.split(",");
                Player p = new Player();

                p.name = line[0];
                p.cost = Integer.valueOf(line[1]);
                p.projection = Double.valueOf(line[2]);
                p.position = line[3];

                Players.add(p);
                numplayers++;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        MainWindow myWindow = new MainWindow();
        myWindow.Players = Players;

        myWindow.setVisible(true);

        

            calculateLineup();

        
        myWindow.finalLineup = finalLineup;
        /*
        //print the entire contents of the built table to a text file for debugging purposes
        File file = new File("fullTable.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(file.getAbsoluteFile());
        } catch (IOException ex) {
            Logger.getLogger(Optimizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedWriter bw = new BufferedWriter(fw);
        
        for(i=0; i<471; i++){
            for(j=0; j<numplayers; j++){
                for(k=0; k<8; k++){
                    bw.write("table[" + i + "][" + j + "][" + k + "] = " + table[i][j][k].points);
                    bw.write("\n");
                }
            }
        }*/                
    }

    public static void calculateLineup() {
        Lineup rbLineup = new Lineup();
        Lineup wrLineup = new Lineup();
        Lineup teLineup = new Lineup();
        table = new Entry[471][numplayers][9];
        int i, j, k;
        
        for(i=0; i<471; i++){
            for(j=0; j<numplayers; j++){
                for(k=0; k<8; k++){
                    table[i][j][k] = new Entry();
                }
            }
        }
        
        for(i=0; i<471; i++){
            for(k=0; k<8; k++){
                if((i*100+3000) >= Players.get(0).cost && k == 0)
                    table[i][0][0].points = Players.get(0).projection;
                else
                    table[i][0][k].points = -500;
            }
        }
        
        for(i=0; i<471; i++){
            for(j=1; j<numplayers; j++){
                if(((Players.get(j).cost-3000)/100) <= i && (getKRBFlex(Players.get(j).position) == 0))
                    table[i][j][0].points = max(table[i][j-1][0].points, Players.get(j).projection);
                else
                    table[i][j][0].points = table[i][j-1][0].points;
            }
        }

        rbLineup = getRBFlex();
        wrLineup = getWRFlex();
        teLineup = getTEFlex();
        
        if(max(rbLineup.points, wrLineup.points) == rbLineup.points){
            if(max(rbLineup.points, teLineup.points) == rbLineup.points)
                finalLineup = rbLineup;
            else
                finalLineup = teLineup;
        }
        else{
            if(max(wrLineup.points,teLineup.points) == wrLineup.points)
                finalLineup = wrLineup;
            else
                finalLineup = teLineup;
        }
        finalLineup.totalCost = getTotalCost(finalLineup);
        /*for(i=8; i>=0; i--){
            System.out.println(finalLineup.players.get(i).name + "," + finalLineup.players.get(i).cost + "," + finalLineup.players.get(i).position + "," + finalLineup.players.get(i).projection);
        }
        
        System.out.println("This point total is " + finalLineup.points + " for a cost of " + finalLineup.totalCost);*/
        
        //return optimal;
    }
    
    public static double getTotalCost(Lineup x){
        int i;
        double tot = 0;
        
        for(i=0; i<x.players.size(); i++)
            tot += x.players.get(i).cost;
        
        return tot;
    }
    
    public static Lineup getRBFlex(){
        int i, j, k;
        Lineup rbLineup = new Lineup();
        ArrayList<Player> rbOptimal = new ArrayList<>();
        
        //rb flex lineup
        for(k=1; k<8; k++){
            for(j=1; j<numplayers; j++){
                for(i=0; i<471; i++){                    
                    if(i-((Players.get(j).cost-3000)/100) < 0){
                        if(i == 0){
                            table[i][j][k].points = 0;
                        }
                        else{
                            table[i][j][k].points = table[i][j-1][k].points;
                        }                        
                    }          
                    
                    //check for case3
                    else if(getPosRBFlex(k).equals(Players.get(j).position)){
                        double val1 = table[i][j-1][k].points;
                        double val2;
                        
                        if((i-(Players.get(j).cost)/100) < 0){
                            table[i][j][k].points = -500;
                            continue;
                        }
                        else if(k == 0){
                            table[i][j][k].points = max(val1, Players.get(j).projection);
                            continue;
                        }
                        else
                            val2 = (Players.get(j).projection + table[i-((Players.get(j).cost)/100)][j-1][k-1].points);
                            
                        
                        table[i][j][k].points = max(val1, val2);

                    }
                    
                    //check for case1
                    else if(getKRBFlex(Players.get(j).position) > getKRBFlex(getPosRBFlex(k))){
                        table[i][j][k].points = table[i][j-1][k].points;                        
                    }
                    
                    //check for case2
                    else if(getKRBFlex(Players.get(j).position) < getKRBFlex(getPosRBFlex(k))){
                        table[i][j][k].points = 0;
                    }
                }
            }
        }
        
        System.out.println("Max pts rb flex is " + table[470][numplayers-1][7].points);
        
        i=470;
        k=7;
        
        for(j=numplayers-1;  j>=0 && k >= 0; j--){
            if(j == 0){
                rbOptimal.add(Players.get(j));
            }
            else if(table[i][j][k].points != table[i][j-1][k].points){
                rbOptimal.add(Players.get(j));
                k--;
                i-=(Players.get(j).cost/100);
            }            
        }
        rbLineup.players = rbOptimal;
        rbLineup.points = table[470][numplayers-1][7].points;
        
        return rbLineup;
    }
    
    public static Lineup getWRFlex(){
        int i, j, k;
        Lineup wrLineup = new Lineup();
        ArrayList<Player> wrOptimal = new ArrayList<>();
        //wr flex lineup
        for(k=1; k<8; k++){
            for(j=1; j<numplayers; j++){
                for(i=0; i<471; i++){                    
                    if(i-((Players.get(j).cost-3000)/100) < 0){
                        if(i == 0){
                            table[i][j][k].points = 0;
                        }
                        else{
                            table[i][j][k].points = table[i][j-1][k].points;
                        }                        
                    }          
                    
                    //check for case3
                    else if(getPosWRFlex(k).equals(Players.get(j).position)){
                        double val1 = table[i][j-1][k].points;
                        double val2;
                        
                        if((i-(Players.get(j).cost)/100) < 0){
                            table[i][j][k].points = -500;
                            continue;
                        }
                        else if(k == 0){
                            table[i][j][k].points = max(val1, Players.get(j).projection);
                            continue;
                        }
                        else
                            val2 = (Players.get(j).projection + table[i-((Players.get(j).cost)/100)][j-1][k-1].points);
                            
                        
                        table[i][j][k].points = max(val1, val2);                        
                    }
                    
                    //check for case1
                    else if(getKWRFlex(Players.get(j).position) > getKWRFlex(getPosWRFlex(k))){
                        table[i][j][k].points = table[i][j-1][k].points;                        
                    }
                    
                    //check for case2
                    else if(getKWRFlex(Players.get(j).position) < getKWRFlex(getPosWRFlex(k))){
                        table[i][j][k].points = 0;
                    }
                }
            }
        }
        
        System.out.println("Max pts wr flex is " + table[470][numplayers-1][7].points);
        
        i=470;
        k=7;
        
        for(j=numplayers-1; j>=0 && k >= 0; j--){
            if(j == 0){
                wrOptimal.add(Players.get(j));
            }
            else if(table[i][j][k].points != table[i][j-1][k].points){
                wrOptimal.add(Players.get(j));
                k--;
                i-=(Players.get(j).cost/100);
            }            
        }
        wrLineup.players = wrOptimal;
        wrLineup.points = table[470][numplayers-1][7].points;
        
        return wrLineup;
    }
    
    public static Lineup getTEFlex(){
        int i, j, k;
        Lineup teLineup = new Lineup();
        ArrayList<Player> teOptimal = new ArrayList<>();
        //te flex lineup
        for(k=1; k<8; k++){
            for(j=1; j<numplayers; j++){
                for(i=0; i<471; i++){                    
                    if(i-((Players.get(j).cost-3000)/100) < 0){
                        if(i == 0){
                            table[i][j][k].points = 0;
                        }
                        else{
                            table[i][j][k].points = table[i][j-1][k].points;
                        }                        
                    }          
                    
                    //check for case3
                    else if(getPosTEFlex(k).equals(Players.get(j).position)){
                        double val1 = table[i][j-1][k].points;
                        double val2;
                        
                        if((i-(Players.get(j).cost)/100) < 0){
                            table[i][j][k].points = -600;
                            continue;
                        }
                        else if(k == 0){
                            table[i][j][k].points = max(val1, Players.get(j).projection);
                            continue;
                        }
                        else
                            val2 = (Players.get(j).projection + table[i-((Players.get(j).cost)/100)][j-1][k-1].points);
                            
                        
                        table[i][j][k].points = max(val1, val2);                        
                    }
                    
                    //check for case1
                    else if(getKTEFlex(Players.get(j).position) > getKTEFlex(getPosTEFlex(k))){
                        table[i][j][k].points = table[i][j-1][k].points;                        
                    }
                    
                    //check for case2
                    else if(getKTEFlex(Players.get(j).position) < getKTEFlex(getPosTEFlex(k))){
                        table[i][j][k].points = 0;
                    }
                }
            }
        }
        
        System.out.println("Max pts TE flex is " + table[470][numplayers-1][7].points);
        
        i=470;
        k=7;
        
        for(j=numplayers-1;  j>=0 && k >= 0; j--){
            if(j == 0){
                teOptimal.add(Players.get(j));
            }
            else if(table[i][j][k].points != table[i][j-1][k].points){
                teOptimal.add(Players.get(j));
                k--;
                i-=(Players.get(j).cost/100);
            }            
        }
        teLineup.players = teOptimal;
        teLineup.points = table[470][numplayers-1][7].points;
        
        return teLineup;
    }
    
    public static String getPos(int k){
        if(k == 0)
            return "QB";
        if(k == 1 || k == 2)
            return "RB";
        if(k == 3 || k == 4)
            return "WR";
        if(k == 5)
            return "TE";
        if(k == 6)
            return "D/ST";
        return "";
    }
    
    public static int getK(String pos){
        if(pos.equals("QB"))
            return 0;
        if(pos.equals("RB"))
            return 1;
        if(pos.equals("WR"))
            return 3;
        if(pos.equals("TE"))
            return 5;
        if(pos.equals("D/ST"))
            return 6;
        return -1;
    }
    public static String getPosRBFlex(int k){
        if(k == 0)
            return "QB";
        if(k == 1 || k == 2 || k == 3)
            return "RB";
        if(k == 4 || k == 5)
            return "WR";
        if(k == 6)
            return "TE";
        if(k == 7)
            return "D/ST";
        return "";
    }
    
    public static int getKRBFlex(String pos){
        if(pos.equals("QB"))
            return 0;
        if(pos.equals("RB"))
            return 1;
        if(pos.equals("WR"))
            return 4;
        if(pos.equals("TE"))
            return 6;
        if(pos.equals("D/ST"))
            return 7;
        return -1;
    }
    public static String getPosWRFlex(int k){
        if(k == 0)
            return "QB";
        if(k == 1 || k == 2)
            return "RB";
        if(k == 3 || k == 4 || k == 5)
            return "WR";
        if(k == 6)
            return "TE";
        if(k == 7)
            return "D/ST";
        return "";
    }
    
    public static int getKWRFlex(String pos){
        if(pos.equals("QB"))
            return 0;
        if(pos.equals("RB"))
            return 1;
        if(pos.equals("WR"))
            return 3;
        if(pos.equals("TE"))
            return 5;
        if(pos.equals("D/ST"))
            return 6;
        return -1;
    }
    public static String getPosTEFlex(int k){
        if(k == 0)
            return "QB";
        if(k == 1 || k == 2)
            return "RB";
        if(k == 3 || k == 4)
            return "WR";
        if(k == 5 || k == 6)
            return "TE";
        if(k == 7)
            return "D/ST";
        return "";
    }
    
    public static int getKTEFlex(String pos){
        if(pos.equals("QB"))
            return 0;
        if(pos.equals("RB"))
            return 1;
        if(pos.equals("WR"))
            return 3;
        if(pos.equals("TE"))
            return 5;
        if(pos.equals("D/ST"))
            return 7;
        return -1;
    }

}
