/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import controlloTraffico.ControlloTraffico;
import controlloTraffico.IR;
import controlloPAI.ControlloPAI;
import controlloIlluminazione.ControlloIlluminazione;
import controlloAccesso.ControlloAccesso;
import DDI.DDI;
import GUI.MainController;
import arduino.LogicTask;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Lorenzo
 */
public class Main extends Application {
    
    public static final ClassToInstanceMap  GUIcontrollers = MutableClassToInstanceMap.create();
    public static Stage stage;
    
    static public MainController controlloGUI;
            
    @Override
    public void start(Stage primaryStage) throws IOException {
        stage=primaryStage;
        System.out.print("Inizializzazione interfaccia grafica... ");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/MainView.fxml"));

        Parent root = loader.load(getClass().getResource("/GUI/MainView.fxml"));
        controlloGUI = (MainController)loader.getController();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        System.out.println("Fine!"); 
        ((MainController)(GUIcontrollers.getInstance(MainController.class))).avviaGUI();
    }

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.print("Inizializzazione dei controller logici... ");
        ControlloTraffico.getInstance();
        IR.getInstance();
        ControlloPAI.getInstance();
        ControlloIlluminazione.getInstance();
        ControlloAccesso.getInstance();
        if(DDI.getInstance()==null)
            System.exit(1);
        
        Thread th = new Thread(LogicTask.getInstance());
        th.setDaemon(true);
        th.start();

        System.out.println("Fine!"); 
        launch(args);
    }
    
}
