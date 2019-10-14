/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab2compiladores;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author 57304
 */
public class Gramatica {
    
    ArrayList<String> producciones;
    ArrayList<String> ProdSinV;
    ArrayList<String> Terminales;
    ArrayList<String> NoTerminales;
    HashMap<String,ArrayList> primeros;
    HashMap<String,ArrayList> siguientes;
    String[][] M;
    
    public Gramatica(ArrayList<String> producciones){
        
        this.NoTerminales = new ArrayList<>();
        this.Terminales = new ArrayList<>();
        this.primeros = new HashMap();
        this.siguientes= new HashMap();
        this.producciones=producciones;
        
    
        //hallar no terminales
        for (int i = 0; i < producciones.size(); i++) {
            //recorrer los que no se han hallado
            if (!this.NoTerminales.contains(producciones.get(i).substring(0,1))) {
                this.NoTerminales.add(producciones.get(i).substring(0,1));
            }    
        }
        
        //hallar terminales
        for (int i = 0; i < producciones.size(); i++) {
            for (int j = 3; j < producciones.get(i).length(); j++) {
                String cadena = producciones.get(i).substring(j, j + 1);

                if (!cadena.equals("&") && !this.NoTerminales.contains(cadena) && !this.Terminales.contains(cadena)) {
                    this.Terminales.add(cadena);
                }
            }
        }
        
        
        System.out.println(this.NoTerminales);
        System.out.println(this.Terminales);
        this.ProdSinV = EliminarVicios();
        primeros();
        siguientes(this.ProdSinV);
        this.M=crearTablaM();
        
    }
    
    
    private ArrayList<String> EliminarVicios(){
        
        //Buscar gramaticas que empiecen con el mismo terimnal
        ArrayList<String> GSV = new ArrayList<>();
        String simbolo = "";
        
        for (int i = 0; i < producciones.size(); i++) {
            if (!producciones.get(i).substring(0, 1).equals(simbolo)) {
                ArrayList<String> GUnica = new ArrayList<>();             
                
                for (int j = 0; j < producciones.size(); j++) {
                    if (producciones.get(i).substring(0, 1).equals(producciones.get(j).substring(0, 1))) {
                        GUnica.add(producciones.get(j));
                    }
                }
                
                System.out.println("GUNICA"+GUnica);
                //Elimina recursividad
                if (!TieneRecursividad(GUnica).isEmpty()) {
                    GUnica = new ArrayList<>(EliminarRecursividad(GUnica, TieneRecursividad(GUnica)));
                }
                //Factoriza
                
                
                //Primeros y siguientes iniciales
                PrimYSgteInicial(GUnica);       
                GSV.addAll(GUnica);  
                simbolo = producciones.get(i).substring(0,1);
            }

        }
        System.out.println(GSV);
        return GSV;

    }
    
    private ArrayList<Integer> TieneRecursividad(ArrayList<String> GUnica) {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < GUnica.size(); i++) {
                if (GUnica.get(i).substring(0, 1).equals(GUnica.get(i).substring(3, 4))) {
                    indices.add(i);
                }
        }
        return indices;
    }

    
    private void PrimYSgteInicial(ArrayList<String> GUnica){
        ArrayList<String> primerosA = new ArrayList();
        ArrayList<String> primerosAP = new ArrayList();
        
        String comprobacionP = GUnica.get(0).substring(0,1);
        String NoTerminal="";

        //llenado de primeros de A 
        for (int i = 0; i < GUnica.size(); i++) {
                if (comprobacionP.equals(GUnica.get(i).substring(0,1))) {
                primerosA.add(GUnica.get(i).substring(3,4));
                }else{
                primerosAP.add(GUnica.get(i).substring(3,4));
                NoTerminal = GUnica.get(i).substring(0, 1);
                }
    
        }
        this.primeros.put(GUnica.get(0).substring(0,1), primerosA);
        this.siguientes.put(GUnica.get(0).substring(0,1), new ArrayList<>());
        
        if (!primerosAP.isEmpty()) {
            this.primeros.put(NoTerminal, primerosAP);
            this.siguientes.put(NoTerminal, new ArrayList<>());
        }
    }
    
    
    
    
    private ArrayList<String> EliminarRecursividad(ArrayList<String> GUnica, ArrayList<Integer> IndicesR){
        ArrayList<String> GSR = new ArrayList();
        
        String NoTerminalInicial = GUnica.get(0).substring(0,1);
     /*"A'"*/   String NoTerminalNuevo = Agregar_NoTerminal();
        
        if (IndicesR.size() == GUnica.size()) {
            //No existe B, por tanto A->A'
            String cadenaBeta = NoTerminalInicial.concat("->").concat(NoTerminalNuevo);
            GSR.add(cadenaBeta);
        }    
        //Crear producciones A->BA'
        for (int i = 0; i < GUnica.size(); i++) {
            String produccion = GUnica.get(i);
            if (!IndicesR.contains(i)) {
                //B
                String Terminal = produccion.substring(3,produccion.length());
                //concateno para crear cadena 
                String cadenaBeta=NoTerminalInicial.concat("->").concat(Terminal).concat(NoTerminalNuevo);
                GSR.add(cadenaBeta);
            }
        }
        //Crear producciones A'->⍺A'
        for (int i = 0; i < IndicesR.size(); i++) {
            String produccion = GUnica.get(IndicesR.get(i));
            //⍺
            String Terminal = produccion.substring(4, produccion.length());
            String cadenaAlfa = NoTerminalNuevo.concat("->").concat(Terminal).concat(NoTerminalNuevo);
            GSR.add(cadenaAlfa);
        }
        
        //agregar nuevo no terminal creado para la recursividad
        
        this.NoTerminales.add(NoTerminalNuevo);
        
        GSR.add(NoTerminalNuevo +"->&");
        
        return GSR;
        
    }
    
    private void Factorizar(){
    
    }
    
    private void primeros(){
        System.out.println("primeros originales");
        this.primeros.forEach((k,v) -> System.out.println("Key: " + k + ": Value: " + v));
        
        for (Map.Entry<String, ArrayList> primero : this.primeros.entrySet()) {
            int i=0;
            boolean flag;
            while (i < primero.getValue().size()) {
                flag = true;
                if (this.primeros.containsKey(primero.getValue().get(i))) {
                    String NoTerminal = primero.getValue().get(i).toString();
                    primero.getValue().remove(i);
                    ArrayList PrimerosN = new ArrayList(primero.getValue());
                    PrimerosN.addAll(this.primeros.get(NoTerminal));
                    this.primeros.replace(primero.getKey(), PrimerosN);
                    flag = false;
                }
                if (flag) {
                    i++;
                }
            }
            
        Set<String> set = new HashSet<>(primero.getValue());
        primero.getValue().clear();
        primero.getValue().addAll(set);
        }
        
        
        System.out.println("nuevos primeros");
        this.primeros.forEach((k,v) -> System.out.println("Key: " + k + ": Value: " + v));
    }
    
    
    private String Agregar_NoTerminal(){
        for (char A = 'A';  A<= 'Z'; A++) {
            if (!this.NoTerminales.contains(String.valueOf(A))) {
                return String.valueOf(A);
            }
        }
        return "XX";
    }
    
    
    private void siguientes(ArrayList<String> producciones){
        System.out.println("siguientes originales");
        this.siguientes.forEach((k,v) -> System.out.println("Key: " + k + ": Value: " + v));
        
        //agrego $ al inicial
        this.siguientes.get(this.NoTerminales.get(0)).add("$");
        this.Terminales.add("$");
        
        for (Map.Entry<String, ArrayList> siguiente : this.siguientes.entrySet()) {
            for (String produccion: producciones) {
                int iProduce = produccion.indexOf(">");
                String cadena = produccion.substring(iProduce + 1, produccion.length());
                if (cadena.contains(siguiente.getKey())) {
    
                    int index = cadena.indexOf(siguiente.getKey());
                    index++;
              
                    if (index != cadena.length()) {
                        
                        String simbolo;                 
                        simbolo = cadena.substring(index, index + 1);
                        
                        //agrego terminal
                        if (this.Terminales.contains(simbolo) && !this.siguientes.get(siguiente.getKey()).contains(simbolo)) {
                            this.siguientes.get(siguiente.getKey()).add(simbolo);
                        }
                        //agrego No terminal
                        if (this.NoTerminales.contains(simbolo) && !this.siguientes.get(siguiente.getKey()).contains(simbolo)) {
                            this.siguientes.get(siguiente.getKey()).add(simbolo);
                        }
                    }else{
                        //verifico el no terminal inicial, si es el mismo no añado nada, pero si no lo es, lo añado y se que sera el sgte
                        String cad = produccion.substring(0,1);
                        if (!siguiente.getKey().equals(cad) && !siguiente.getValue().contains("sgt"+cad)) {
                            this.siguientes.get(siguiente.getKey()).add("sgt"+cad);
                        }
                        
                    }

                }
            }
        }
        
        System.out.println("");
        this.siguientes.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
        System.out.println("");
        
        while (checksiguiente()) {
            for (Map.Entry<String, ArrayList> siguiente : this.siguientes.entrySet()) {
                for (int i = 0; i < siguiente.getValue().size(); i++) {
                    String elemento = siguiente.getValue().get(i).toString();

                    //caso E->⍺C , se añadre PRIM(C)
                    if (this.NoTerminales.contains(elemento)) {
                        siguiente.getValue().remove(i);
                        siguiente.getValue().addAll(this.primeros.get(elemento));
                        //Caso en que el primero sea &, se añade el sgte
                        if (this.primeros.get(elemento).contains("&")) {
                            siguiente.getValue().remove("&");
                            siguiente.getValue().addAll(this.siguientes.get(elemento));
                        }
                        Set<String> set = new HashSet<>(siguiente.getValue());
                        siguiente.getValue().clear();
                        siguiente.getValue().addAll(set);
                    }

                    //caso E->⍺E' , donde SGTE(E')->SGTE(E)
                    if (elemento.length() == 4) {
                        if (elemento.substring(0, 3).equals("sgt")) {
                            String NoTerminal = elemento.substring(3, 4);
                            siguiente.getValue().remove(i);
                            ArrayList<String> sig = new ArrayList(this.siguientes.get(NoTerminal));
                            siguiente.getValue().addAll(sig);
                            
                            Set<String> set = new HashSet<>(siguiente.getValue());
                            siguiente.getValue().clear();
                            siguiente.getValue().addAll(set);
                        }
                    }
                    
                }
            }
        }
        
        System.out.println("nuevos siguientes");
        this.siguientes.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
        System.out.println("");

    }
    
    private boolean checksiguiente() {
        boolean flag = false;
        for (ArrayList<String> siguientes : this.siguientes.values()) {
            for (int i = 0; i < siguientes.size(); i++) {
                if (this.NoTerminales.contains(siguientes.get(i))||siguientes.get(i).length()==4) {
                    flag = true;
                }
            }
        }

        return flag;
    }
    
    
    private String [][] crearTablaM(){
     
    int filas = this.NoTerminales.size();
    int col = this.Terminales.size();
    
    String [][] M = new String[filas+1][col+1]; 
    M[0][0] = "NoTerm/Term";
    
        for (int i = 0; i < col; i++) {
            M[0][i+1]= this.Terminales.get(i);
        }
    
        for (int i = 0; i < filas; i++) {
            M[i+1][0] = this.NoTerminales.get(i);
        }
        
     
        //Añade primeros y siguiente(&)
        for (int i = 0; i < filas ; i++) {
            String NT = M[i+1][0];
            for (int j = 0; j < col; j++) {
                String T = M[0][j+1];
                if (this.primeros.get(NT).contains(T)) {
                    for (String produccion: this.ProdSinV) {
                        if (produccion.substring(3, 4).equals(T) && produccion.substring(0,1).equals(NT)) {
                             M[i+1][j+1] = produccion;
                        }
                    }
                }
            }
            if (this.primeros.get(NT).contains("&")) {
                for (int j = 0; j < col; j++) {
                    if (this.siguientes.get(NT).contains(M[0][j+1])) {
                       M[i+1][j+1] = NT+"->"+"&";
                    }
                }
            }
        }
   
           for (int i = 0; i < filas+1; i++) {
            for (int j = 0; j < col+1; j++) {
                System.out.print(M[i][j] + "|");
            }
            System.out.println("");
        }
        

       return M; 
        
    
    }
    

}
