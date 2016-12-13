/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import controlloAccesso.ControlloAccesso;
import controlloAccesso.Funzione;
import controlloIlluminazione.ControlloIlluminazione;
import controlloIlluminazione.Criterio;
import eccezioni.PAIAttivaException;
import eccezioni.PermessoInsufficienteException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.Initializable;

/**
 *
 * @author Lorenzo
 */
public abstract class SettoreController{
    
    

    /**
     * Instaura un'associazione tra una serie RadioButton appartenenti all'interfaccia
     * di tale settore ed una serie di enum.
     */
    abstract void associaPulsantiAdEnum();

    /**
     * Richiede al controller logico sottostante la sua attuale configurazione al fine
     * di poterla adottare.
     */
    abstract void recuperaImpostazioniInUso();

    /**
     * Richiede al controller logico sottostante di adottare la configurazione attualmente
     * contenuto in questo settore dell'interfaccia.
     */
    abstract void adottaNuoveImpostazioni();

    /**
     * Abilita o disabilita gli input dell'utente su tale settore dell'interfaccia.
     * @param val 
     */
    abstract void disabilitaVista(boolean val) ;

    /**
     * Consente di richiamare il metodo richiediFunzione di ControlloAccesso catturando
     * automaticamente le eccezioni.
     * @param pF La Funzione che si vuole richiedere
     * @param pO Parametro da passare alla funzione
     * @return <code>true</code> se non sono state catturate eccezioni, <code>false</code> altrimenti.
     * @see ControlloAccesso#richiediFunzione(controlloAccesso.Funzione, java.lang.Object) 
     */
    protected boolean richiediFunzioneSafely(Funzione pF, Object pO) {
        try {
            ControlloAccesso.getInstance().richiediFunzione(pF, pO);
        } catch (PermessoInsufficienteException | PAIAttivaException ex) {
            Logger.getLogger(TrafficoController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }    
}
