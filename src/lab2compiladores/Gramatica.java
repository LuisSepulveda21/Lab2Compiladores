/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab2compiladores;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author 57304
 */
public class Gramatica {

    ArrayList<String> producciones;
    ArrayList<String> ProdSinV;
    ArrayList<String> Terminales;
    ArrayList<String> NoTerminales;
    LinkedHashMap<String, ArrayList> primeros;
    LinkedHashMap<String, ArrayList> siguientes;
    String[][] M;
    int menorCad;

    public Gramatica(ArrayList<String> producciones) {

        this.NoTerminales = new ArrayList<>();
        this.Terminales = new ArrayList<>();
        this.primeros = new LinkedHashMap();
        this.siguientes = new LinkedHashMap();
        this.producciones = producciones;

        //hallar no terminales
        for (int i = 0; i < producciones.size(); i++) {
            //recorrer los que no se han hallado
            if (!this.NoTerminales.contains(producciones.get(i).substring(0, 1))) {
                this.NoTerminales.add(producciones.get(i).substring(0, 1));
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

        //System.out.println(this.NoTerminales);
        //System.out.println(this.Terminales);
        this.ProdSinV = EliminarVicios();
        primeros();
        siguientes(this.ProdSinV);
        this.M = crearTablaM();

    }

    private ArrayList<String> EliminarVicios() {

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

                //System.out.println("GUNICA"+GUnica);
                //Elimina recursividad
                if (!TieneRecursividad(GUnica).isEmpty()) {
                    GUnica = new ArrayList<>(EliminarRecursividad(GUnica, TieneRecursividad(GUnica)));
                }

                //System.out.println("GUNICA SIN REC "+GUnica);
                //Factoriza
                while (!factorizable(GUnica).isEmpty()) {
                    GUnica = new ArrayList<>(factorizar(factorizable(GUnica), GUnica));
                }

                //Primeros y siguientes iniciales
                PrimYSgteInicial(GUnica);
                GSV.addAll(GUnica);
                simbolo = producciones.get(i).substring(0, 1);
            }

        }
        //System.out.println(GSV);
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

    private void PrimYSgteInicial(ArrayList<String> GUnica) {
        ArrayList<String> PriSigIniciales = new ArrayList();

        PriSigIniciales.add(GUnica.get(0).substring(0, 1));
        for (int i = 0; i < GUnica.size(); i++) {
            for (int j = 0; j < GUnica.size(); j++) {
                if (!GUnica.get(i).substring(0, 1).equals(GUnica.get(j).substring(0, 1))) {
                    PriSigIniciales.add(GUnica.get(i).substring(0, 1));
                }
            }
        }
        PriSigIniciales = removeDuplicates(PriSigIniciales);

        //System.out.println("nuevos no t para prim y sig: " + PriSigIniciales);
        for (int i = 0; i < PriSigIniciales.size(); i++) {
            this.primeros.put(PriSigIniciales.get(i), new ArrayList<>());
            this.siguientes.put(PriSigIniciales.get(i), new ArrayList<>());
        }

        //llenado de primeros de A 
        for (int i = 0; i < GUnica.size(); i++) {
            //System.out.println(GUnica.get(i));
            this.primeros.get(GUnica.get(i).substring(0, 1)).add(GUnica.get(i).substring(3, 4));
        }

    }

    public static <String> ArrayList<String> removeDuplicates(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<String>();
        for (String element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }

        return newList;
    }

    private ArrayList<String> EliminarRecursividad(ArrayList<String> GUnica, ArrayList<Integer> IndicesR) {
        ArrayList<String> GSR = new ArrayList();

        String NoTerminalInicial = GUnica.get(0).substring(0, 1);
        /*"A'"*/ String NoTerminalNuevo = Agregar_NoTerminal();

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
                String Terminal = produccion.substring(3, produccion.length());
                //concateno para crear cadena 
                String cadenaBeta = NoTerminalInicial.concat("->").concat(Terminal).concat(NoTerminalNuevo);
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

        GSR.add(NoTerminalNuevo + "->&");

        return GSR;

    }

    private void primeros() {
        System.out.println("primeros originales");
        this.primeros.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));

        for (Map.Entry<String, ArrayList> primero : this.primeros.entrySet()) {
            int i = 0;
            boolean flag;
            while (i < primero.getValue().size()) {
                flag = true;
                if (this.primeros.containsKey(primero.getValue().get(i))) {
                    String NoTerminal = primero.getValue().get(i).toString();
                    primero.getValue().remove(i);
                    ArrayList PrimerosN = new ArrayList(primero.getValue());

                    PrimerosN.addAll(this.primeros.get(NoTerminal));

                    if (this.primeros.get(NoTerminal).contains("&")) {
                        String ProdR = null;
                        for (String produccion : ProdSinV) {
                            if (produccion.substring(0, 1).equals(primero.getKey()) && produccion.substring(3, 4).equals(NoTerminal) && produccion.length() != 4) {
                                ProdR = produccion;
                            }
                            if (ProdR != null) {
                                int i1 = 4;
                                int i2 = 5;
                                while (i1 < ProdR.length()) {
                                    if (this.Terminales.contains(ProdR.substring(i1, i2))) {
                                        PrimerosN.add(ProdR.substring(i1, i2));
                                        PrimerosN.removeAll(Collections.singleton("&"));
                                        i1 = 100;
                                    } else {
                                        if (this.NoTerminales.contains(ProdR.substring(i1, i2))) {
                                            NoTerminal = ProdR.substring(i1, i2);
                                            PrimerosN.addAll(this.primeros.get(NoTerminal));
                                            if (this.primeros.get(NoTerminal).contains("&")) {
                                                i1++;
                                                i2++;
                                            } else {
                                                i1 = 100;
                                            }
                                        }
                                    }
                                }

                            }

                        }

                    }

                    /*      String compvacio = "";
                    
                     if (this.primeros.get(NoTerminal).contains("&")) {
                     compvacio = "&";
                     }
                    
                     while(compvacio.equals("&")){
                     if (this.primeros.containsKey(i)) {
                            
                     }
                     } */
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
        this.primeros.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
    }

    private String Agregar_NoTerminal() {
        for (char A = 'A'; A <= 'Z'; A++) {
            if (!this.NoTerminales.contains(String.valueOf(A))) {
                return String.valueOf(A);
            }
        }
        return "XX";
    }

    private void siguientes(ArrayList<String> producciones) {
        //System.out.println("siguientes originales");
        //this.siguientes.forEach((k,v) -> System.out.println("Key: " + k + ": Value: " + v));

        //agrego $ al inicial
        //System.out.println("ahskhjasjk"+this.NoTerminales);
        this.siguientes.get(this.NoTerminales.get(0)).add("$");
        this.Terminales.add("$");

        for (Map.Entry<String, ArrayList> siguiente : this.siguientes.entrySet()) {
            for (String produccion : producciones) {
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
                    } else {
                        //verifico el no terminal inicial, si es el mismo no añado nada, pero si no lo es, lo añado y se que sera el sgte
                        String cad = produccion.substring(0, 1);
                        if (!siguiente.getKey().equals(cad) && !siguiente.getValue().contains("sgt" + cad)) {
                            this.siguientes.get(siguiente.getKey()).add("sgt" + cad);
                        }

                    }

                }
            }
        }

        System.out.println("Siguientes primeros");
        this.siguientes.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
        //System.out.println("");

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

        //System.out.println("nuevos siguientes");
        //this.siguientes.forEach((k, v) -> System.out.println("Key: " + k + ": Value: " + v));
        //System.out.println("");
    }

    private boolean checksiguiente() {
        boolean flag = false;
        for (ArrayList<String> siguientes : this.siguientes.values()) {
            for (int i = 0; i < siguientes.size(); i++) {
                if (this.NoTerminales.contains(siguientes.get(i)) || siguientes.get(i).length() == 4) {
                    flag = true;
                }
            }
        }

        return flag;
    }

    private String[][] crearTablaM() {

        int filas = this.primeros.size();
        int col = this.Terminales.size();

        String[][] M = new String[filas + 1][col + 1];
        M[0][0] = "NT/T";

        for (int i = 0; i < col; i++) {
            M[0][i + 1] = this.Terminales.get(i);
        }

        int w = 0;
        for (Map.Entry<String, ArrayList> primero : this.primeros.entrySet()) {
            M[w + 1][0] = primero.getKey();
            w++;
        }

        //Añade primeros y siguiente(&)
        for (int i = 0; i < filas; i++) {
            String NT = M[i + 1][0];
            for (int j = 0; j < col; j++) {
                String T = M[0][j + 1];
                if (this.primeros.get(NT).contains(T)) {
                    for (String produccion : this.ProdSinV) {
                        //System.out.println("prod de: "+produccion + " Noter " + NT + " ter " + T);
                        if (produccion.substring(3, 4).equals(T) && produccion.substring(0, 1).equals(NT)) {
                            M[i + 1][j + 1] = produccion;
                        } else {
                            if (this.NoTerminales.contains(produccion.substring(3, 4)) && produccion.substring(0, 1).equals(NT)) {
                                if (this.primeros.get(produccion.substring(3, 4)).contains(T)) {
                                    M[i + 1][j + 1] = produccion;
                                } else {
                                    if (produccion.length() > 4 && this.primeros.get(produccion.substring(3, 4)).contains("&")) {
                                        int i1 = 4;
                                        int i2 = 5;
                                        while (i1 < produccion.length()) {
                                            String elemento = produccion.substring(i1, i2);
                                            
                                            if (elemento.equals(T) || this.primeros.get(elemento).contains(T)) {
                                                M[i + 1][j + 1] = produccion;
                                                i1 = 100;
                                            } else {
                                                if (this.primeros.get(elemento).contains("&")) {
                                                    i1++;
                                                    i2++;
                                                } else {
                                                    i1 = 100;
                                                }
                                            }

                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
            if (this.primeros.get(NT).contains("&")) {
                for (int j = 0; j < col; j++) {
                    if (this.siguientes.get(NT).contains(M[0][j + 1]) && M[i+1][j+1]==null) {
                        M[i + 1][j + 1] = NT + "->" + "&";
                    }
                }
            }
        }

        /*for (int i = 0; i < filas+1; i++) {
         for (int j = 0; j < col+1; j++) {
         System.out.print(M[i][j] + "|");
         }
         System.out.println("");
         }*/
        return M;

    }

    private HashMap<Integer, ArrayList<Integer>> factorizable(ArrayList<String> GUnica) {
        //la llave sera el menor de los indices y el array que producciones tienen iguales
        HashMap<Integer, ArrayList<Integer>> IndIguales = new HashMap();

        //me permite saber la mnenor ocurrencia
        int menornum = 100;
        int menornum2 = 0;
        for (int i = 0; i < GUnica.size(); i++) {
            ArrayList<Integer> Inicial = new ArrayList();
            Inicial.add(i);

            //la key tendra la menor ocurrencia y el array los indices de las ocurrencias
            IndIguales.put(menornum, Inicial);
            String produccion = GUnica.get(i);
            int iProduce = produccion.indexOf(">");
            String cadenaI = produccion.substring(iProduce + 1, GUnica.get(i).length());

            if (!cadenaI.equals("&")) {
                for (int j = 0; j < GUnica.size(); j++) {
                    String produccion2 = GUnica.get(j);
                    if (produccion.equals(produccion2) || !produccion.substring(0, 1).equals(produccion2.substring(0, 1))) {
                        continue;
                    }

                    menornum2 = 0;
                    String cadenaJ = produccion2.substring(iProduce + 1, produccion2.length());

                    //quien define la longitud
                    int longitud;
                    if (cadenaI.length() <= cadenaJ.length()) {
                        longitud = cadenaI.length();
                    } else {
                        longitud = cadenaJ.length();
                    }

                    for (int k = 0; k < longitud; k++) {
                        boolean flag = true;
                        if (cadenaI.substring(k, k + 1).equals(cadenaJ.substring(k, k + 1))) {
                            menornum2++;
                            flag = false;
                        }
                        //debe encontrar en cadena, ej: ababa igual ababa y no: ababa igual bbaaa, encontraria en 2,3 y 4 pero no en cadena
                        if (flag) {
                            break;
                        }
                    }
                    //añade donde encontro la ocurrencia
                    if (menornum2 > 0) {
                        IndIguales.get(menornum).add(j);
                    }

                    //menornum2 me saca los que encontro en cadena
                    if (menornum2 <= menornum && menornum2 != 0) {
                        IndIguales.put(menornum2, IndIguales.remove(menornum));
                        menornum = menornum2;
                        menorCad = j;
                        //System.out.println(menorCad);
                        //System.out.println("j"+j);
                        //System.out.println(IndIguales);
                    }

                }
            }
            if (menornum != 100) {
                if (GUnica.get(i).length() < GUnica.get(menorCad).length()) {
                    menorCad = i;
                }
                return IndIguales;
            }
        }
        IndIguales.clear();
        return IndIguales;
    }

    private ArrayList<String> factorizar(HashMap<Integer, ArrayList<Integer>> IndIguales, ArrayList<String> GUnica) {
        /*"A'"*/ String NoTerminalNuevo = Agregar_NoTerminal();
        Iterator iter = IndIguales.entrySet().iterator();
        Map.Entry<Integer, ArrayList<Integer>> entry = (Map.Entry<Integer, ArrayList<Integer>>) iter.next();

        String cadenaN = new String();
        boolean maniq = false;
        String cadenaI = "";

        //System.out.println("ARRAY ORIGINAL "+ GUnica);
        for (int i = 0; i < entry.getValue().size(); i++) {
            int num_c = entry.getValue().get(i);
            String cadena_o = GUnica.get(num_c);
            int start = entry.getKey();
            if (num_c == menorCad) {
                cadenaI = cadena_o.substring(0, 3 + start) + NoTerminalNuevo;
                if (cadena_o.substring(0, 3 + start).length() == cadena_o.length()) {
                    maniq = true;
                } else {
                    cadenaN = NoTerminalNuevo + "->" + cadena_o.substring(3 + start, cadena_o.length());
                    GUnica.add(cadenaN);
                }
                continue;
            }
            //System.out.println("num_c " + num_c + "cadena " + cadena_o);
            cadenaN = NoTerminalNuevo + "->" + cadena_o.substring(3 + start, cadena_o.length());
            GUnica.set(num_c, cadenaN);
        }
        if (maniq) {
            GUnica.add(NoTerminalNuevo + "->&");
        }

        GUnica.remove(menorCad);
        ArrayList<String> nGUnica = new ArrayList();
        nGUnica.add(cadenaI);
        nGUnica.addAll(GUnica);

        this.NoTerminales.add(NoTerminalNuevo);
        //System.out.println("NUEVO ARRAY: " + nGUnica);

        return nGUnica;
    }

    public void reconocerG(String cadena, DefaultTableModel SegModel) {
        cadena = cadena + "$";
        char[] cad = cadena.toCharArray();
        int index = 0;

        Stack<String> pila = new Stack();
        pila.push("$");
        pila.push(this.NoTerminales.get(0));

        do {
            //X es el simbolo de la cima de la pila
            System.out.println("pila " + pila);
            String entrada = "";
            for (int i = index; i < cad.length; i++) {
                entrada += cad[i];
            }
            String mypila = "";

            Iterator<String> itr = pila.iterator();
            while (itr.hasNext()) {
                mypila += itr.next();
                System.out.println(mypila);
            }

            System.out.println(entrada);
            String X = pila.peek().toString();
            if (this.Terminales.contains(X) || X.equals("$")) {
                if (X.equals(String.valueOf(cad[index]))) {
                    //extraer X
                    SegModel.addRow(new Object[]{mypila, entrada, ""});
                    pila.pop();
                    //avanzar a
                    index++;
                } else {
                    SegModel.setRowCount(0);
                    JOptionPane.showMessageDialog(null, "Cadena no aceptada");
                    break;
                }
            } else {
                //X es un no terminal
                //hallo M[X,a]
                int indx = 0;
                int inda = 0;
                boolean flag = true;

                int indice = 1;
                while (flag && indice <= this.NoTerminales.size()) {
                    if (this.M[indice][0].equals(X)) {
                        //System.out.println(this.M[indice][0]);
                        flag = false;
                        indx = indice;
                    }
                    indice++;
                }
                if (flag) {
                    SegModel.setRowCount(0);
                    JOptionPane.showMessageDialog(null, "Cadena no aceptada");
                    break;
                }
                flag = true;
                indice = 1;
                while (flag && indice <= this.Terminales.size()) {
                    if (this.M[0][indice].equals(String.valueOf(cad[index]))) {
                        //System.out.println(this.M[0][indice]);
                        flag = false;
                        inda = indice;
                    }
                    indice++;
                }
                if (flag) {
                    SegModel.setRowCount(0);
                    JOptionPane.showMessageDialog(null, "Cadena no aceptada");
                    break;
                }

                String CadM = this.M[indx][inda];
                //System.out.println(CadM);

                if (!CadM.isEmpty()) {
                    System.out.println(CadM);
                    SegModel.addRow(new Object[]{mypila, entrada, CadM});
                    pila.pop();
                    int len = CadM.length();
                    String cadenapila = CadM.substring(3, len);
                    int RevIndex = cadenapila.length() - 1;
                    char[] cadrev = cadenapila.toCharArray();
                    while (RevIndex >= 0) {
                        pila.push(String.valueOf(cadrev[RevIndex]));
                        RevIndex--;
                    }
                }
                if (pila.peek().equals("&")) {
                    pila.pop();
                }
            }
        } while (!pila.peek().equals("$"));

        if (String.valueOf(cad[index]).equals("$") && pila.peek().equals("$")) {
            SegModel.addRow(new Object[]{"$", "$", "Aceptada"});
            System.out.println("cadena aceptada");
        } else {
            SegModel.setRowCount(0);
            JOptionPane.showMessageDialog(null, "Cadena no aceptada");
        }

    }

}
