/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APP.controlloAccesso;

/**
 * Quest'enumerazione codifica le diverse funzioni fornite all'utente.
 */
public enum Funzione {
    // Illuminazione
    SET_CRITERIO,
    SET_LIVELLO_CM,
    
    // PAI
    DISATTIVA_PAI,
    
    // Traffico
    SET_CIRCOLAZIONE,
    SET_DURATA_V_SX,
    SET_DURATA_V_DX,
    SET_DURATA_R_AGG;
}
