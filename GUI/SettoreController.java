package GUI;

import APP.controlloAccesso.ControlloAccesso;
import APP.controlloAccesso.Funzione;
import APP.eccezioni.FunzioneNonDisponibileException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;

/**
 * Classe astratta che mette a fattor comune la strategia di implementazione e la
 * gestione dei controller grafici. Trovandoci in ambiente JavaFX, implementa Initializable.
 */
public abstract class SettoreController implements Initializable{
    /**
     * Il contenitore grafico principale del settore in questione.
     * Il settore effettivo che estenderà questa classe dovrà assicurarsi di assegnargli, nell'atto
     * dell'inizializzazione, il contenitore principale.
     * @see Initializable#initialize(java.net.URL, java.util.ResourceBundle) .
     */
    protected Pane paneSettore;

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
     * @param val <code>True/False</code> se va disabilitato/abilitato.
     */
    protected void disabilitaVista(boolean val){
        paneSettore.disableProperty().set(val);
    }

    /**
     * Consente di richiamare il metodo richiediFunzione di ControlloAccesso catturando
     * automaticamente le eccezioni.
     * @param pF La Funzione che si vuole richiedere
     * @param pO Parametro da passare alla funzione
     * @return <code>true</code> se la chiamata è andata a buon fine e dunque non sono state catturate eccezioni, <code>false</code> altrimenti.
     * @see ControlloAccesso#richiediFunzione(controlloAccesso.Funzione, java.lang.Object) 
     */
    protected boolean richiediFunzioneSafely(Funzione pF, Object pO) {
        try {
            ControlloAccesso.getInstance().richiediFunzione(pF, pO);
        } catch (FunzioneNonDisponibileException ex) {
            Logger.getLogger(SettoreController.class.getName()).log(Level.WARNING, "Funzione -> "+pF, ex);
            return false;
        }
        return true;
    }    
}
