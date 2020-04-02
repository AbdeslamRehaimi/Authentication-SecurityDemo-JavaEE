/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import entities.Authentification;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author TOSHIBA
 */
@Stateless
public class AuthentificationFacade extends AbstractFacade<Authentification> {

    @PersistenceContext(unitName = "SecurityAuthetification-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AuthentificationFacade() {
        super(Authentification.class);
    }
    
}
