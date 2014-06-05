import java.io.*;

public class Pipe {
    public static void main(String entrada[]) throws IOException{
        // Ejecutar programas ambiente y agente
        String argsAmbiente[] = entrada[0].split("\\s");
        String argsAgente[] = entrada[1].split("\\s");
        
        Process procesoAmbiente = new ProcessBuilder(argsAmbiente).start();
        Process procesoAgente = new ProcessBuilder(argsAgente).start();
        
        // Obtener entradas y salidas
        BufferedReader salidaAmb = new BufferedReader(
                                    new InputStreamReader(
                                        procesoAmbiente.getInputStream()));
        BufferedReader salidaAgente = new BufferedReader(
                                        new InputStreamReader(
                                           procesoAgente.getInputStream()));


        BufferedReader errorAgente = new BufferedReader(
                                        new InputStreamReader(
                                           procesoAgente.getErrorStream()));

        
        PrintStream entradaAmb = new PrintStream(
                                    procesoAmbiente.getOutputStream());
        PrintStream entradaAgente = new PrintStream(
                                       procesoAgente.getOutputStream());

        String mensaje = null;
        while (!procesoTerminado(procesoAmbiente)) {
            if (salidaAmb.ready()) { // Ambiente envia mensaje a Agente
                mensaje = salidaAmb.readLine();
                System.out.println("Ambiente -> Agente: " + mensaje);
                entradaAgente.println(mensaje);
                entradaAgente.flush();
            }
            if (salidaAgente.ready()) { // Agente envia mensaje a Ambiente
                mensaje = salidaAgente.readLine();
                System.out.println("Agente -> Ambiente: " + mensaje);
                entradaAmb.println(mensaje);
                entradaAmb.flush();
            }
            if (errorAgente.ready()) { // Agente envia mensaje a Ambiente
                mensaje = errorAgente.readLine();
                System.out.println("ERROR: " + mensaje);
                entradaAmb.println(mensaje);
                entradaAmb.flush();
            }
        }
    }

    private static boolean procesoTerminado (Process proceso) {
        try {
            proceso.exitValue();
        } catch (IllegalThreadStateException itse) {
            return false;
        }
        return true;
    }
    
}