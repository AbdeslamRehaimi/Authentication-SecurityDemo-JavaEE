/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AuthentificationController;

import Securiter.SecureSHA1;
import entities.Authentification;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import models.AuthentificationFacade;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author TOSHIBA
 */
@Named(value = "loginController")
@RequestScoped
public class loginController {

    @EJB
    private AuthentificationFacade authentificationFacade;

    /**
     * Creates a new instance of loginController
     */
    Authentification authentification = new Authentification();
    Authentification connectedUser = new Authentification();
    private String passwordVerification;
    EntityManager em;
    EntityManagerFactory emf;
    String renederedSuivantBtn = "false";
    String confirmationCode;
    ServletRequest request;
    ServletResponse response;
    private String adminVisible = "true";
    private String userVisible = "true";
    private String visitorVisible = "true";

    public loginController() {
        emf = Persistence.createEntityManagerFactory("SecurityAuthetification-ejbPU");
        em = emf.createEntityManager();
    }

    public void login() throws IOException {
        System.err.println("REAAAAAAAAACHED");
        boolean existence = false;
        FacesContext context = FacesContext.getCurrentInstance();
        System.err.println("email! " + authentification.getEmail() + " pass! " + authentification.getPassword());
        if (authentification.getPassword() != null && authentification.getEmail() != null) {
            if (authentification.getPassword().length() < 8) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erreur: Password length < 8 oubien les password non identique"));
                System.out.println("Password < 8");
            } else {
                try {
                    System.out.println("Begiiiiiiiiiiiiiiiiin");
                    String HashPass2 = SecureSHA1.getSHA1(authentification.getPassword());
                    connectedUser = em.createNamedQuery("Authentification.Login", Authentification.class)
                            .setParameter("email", authentification.getEmail()).setParameter("password", HashPass2).getSingleResult();
                    if (connectedUser.getNom() != null) {
                        existence = true;
                        System.out.print("You are: " + connectedUser.getNom() + " " + connectedUser.getPrenom());
                    } else {
                        connectedUser = new Authentification();
                        authentification = new Authentification();
                        existence = false;
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Aucun utilisateur pour ces identifiant !"));
                    }
                } catch (Exception e) {
                    System.out.println("erreur: " + e.getMessage());
                }
            }
        }
        System.out.println(existence);

        if (existence) {
            String msg = "Bienvenu: ";
            if (connectedUser.getRole().equals("Admin")) {
                System.out.println("Reached for Admin");
                this.userVisible = "true";
                this.visitorVisible = "true";
                this.adminVisible = "true";
            } else if (connectedUser.getRole().equals("User")) {
                System.out.println("Reached for User");
                this.userVisible = "false";
                this.visitorVisible = "true";
                this.adminVisible = "true";
            } else if (connectedUser.getRole().equals("Visitor")) {
                System.out.println("Reached for Visitor");
                this.userVisible = "false";
                this.visitorVisible = "false";
                this.adminVisible = "true";
            } else {
                this.userVisible = "true";
                this.visitorVisible = "true";
                this.adminVisible = "true";
            }
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("User", connectedUser);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("IDENTITY", connectedUser.getRole());
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("msgWm", msg);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("Nom", connectedUser.getNom() + 
                    " " + connectedUser.getPrenom());
            //return "home.jsf?faces-redirect=true";
            context.getExternalContext().getFlash().setKeepMessages(true);
            context.getExternalContext().redirect("home.jsf");
        } else {
            connectedUser = new Authentification();
            authentification = new Authentification();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Aucun utilisateur pour ces identifiant !"));
            //return "login.jsf?faces-redirect=true";
            context.getExternalContext().getFlash().setKeepMessages(true);
            context.getExternalContext().redirect("login.jsf");
        }

    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("User");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("Nom");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index.jsf?faces-redirect=true";
    }

    public void register() throws Exception {
        String HashPass;
        String k = null;
        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("****************Check Email Exist********************");
        for (int i = 0; i < findAll().size(); i++) {
            System.out.println(findAll().get(i).getEmail().toString());
            if (authentification.getEmail().equalsIgnoreCase(findAll().get(i).getEmail())) {
                k = "Email deja exist";
                break;
            } else {
                k = null;
            }
        }
        System.out.println("*****************End Check************************");
        System.out.println("result was: " + k + " email passed: " + authentification.getEmail() + " conf! " + this.passwordVerification);

        System.out.println("L'etat de cette utilisateur: " + k+"       " + authentification.getPassword().length());
        System.out.println("*******************Test**********************");
        
        if(k != null){
            //k is a variable qui detecter si un email deja exist ou non !!! null sinon else notnull
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Eror: this email is already claimed by someone! !", ""));
        }else{
            if(authentification.getPassword().length()<8 || this.passwordVerification.length()<8){
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Eror: password lenght is < 8", ""));
            }else if(!authentification.getPassword().matches(this.passwordVerification)){
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Eror: Password not match", ""));
            }else{
                try {
                    HashPass = SecureSHA1.getSHA1(authentification.getPassword());
                    System.err.println("____________________try using hash___________________");
                    System.err.println(HashPass);
                    System.err.println(authentification.getPassword());
                    authentification.setPassword(HashPass);
                    System.err.println(HashPass);
                    System.err.println(authentification.getPassword());
                    this.authentificationFacade.create(authentification);
                 } catch (Exception e) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, e.getMessage(), ""));
                } finally {
                    authentification = new Authentification();
                    this.passwordVerification = null;
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "You are registred succefully", ""));
                }
            }
            
        }

    }

    public String passwordRecover() throws Exception {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("password");
        renederedSuivantBtn = "false";

        HttpServletRequest req1 = (HttpServletRequest) request;
        HttpServletResponse res1 = (HttpServletResponse) response;
        try {
            connectedUser = (Authentification) em.createNamedQuery("Authentification.findByEmail", Authentification.class).setParameter("email", authentification.getEmail()).getSingleResult();
            String unHashPass = SecureSHA1.getSHA1(connectedUser.getPassword());
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("msg", "Votre code de confirmation est: ");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("code", "CDN-" + unHashPass.substring(3, 8) + "-CDN");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("email", connectedUser.getEmail());
            renederedSuivantBtn = "true";

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            authentification = new Authentification();

            //res1.sendRedirect(req1.getContextPath()+"/password-update.jsf" );
        }
        return "password-update.jsf?faces-redirect=true";
    }

    public void updatePassword() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            String mainCodeDonner = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("code").toString();
            System.out.println("Code: " + mainCodeDonner);
            System.out.println("Main Password: " + authentification.getPassword());
            System.out.println("Conf Pass: " + passwordVerification);
            System.out.println("User passed code: " + confirmationCode);
            String email = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("email").toString();
            connectedUser = (Authentification) em.createNamedQuery("Authentification.findByEmail", Authentification.class).setParameter("email", email).getSingleResult();

            System.out.println(connectedUser.getId() + " " + connectedUser.getEmail());
            if (confirmationCode.equals(mainCodeDonner) && authentification.getPassword().equals(passwordVerification)) {
                try {
                    Authentification auth = new Authentification();
                    auth = authentificationFacade.find(connectedUser.getId());
                    auth.setPassword(SecureSHA1.getSHA1(passwordVerification));
                    ///connectedUser.setPassword(passwordVerification);
                    authentificationFacade.edit(auth);
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Update Sussces!", "."));

                    //context.getExternalContext().getFlash().setKeepMessages(true);
                    //context.getExternalContext().redirect("login.jsf");
                    this.passwordVerification = null;
                    this.authentification = new Authentification();
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("code");
                } catch (Exception e) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));

                }

            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Unkown!"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void seletct(Authentification e) {
        this.authentification = e;
    }

    public void delete(Authentification aut) {
        try {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Delete Succesful!", "Unkown!"));
            this.authentificationFacade.remove(aut);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), ""));
        } finally {
            aut = new Authentification();
        }
    }

    public List<Authentification> findAll() {
        return this.authentificationFacade.findAll();
    }

    public Authentification getAuthentification() {
        return authentification;
    }

    public void setAuthentification(Authentification authentification) {
        this.authentification = authentification;
    }

    public String getPasswordVerification() {
        return passwordVerification;
    }

    public void setPasswordVerification(String passwordVerification) {
        this.passwordVerification = passwordVerification;
    }

    public String getRenederedSuivantBtn() {
        return renederedSuivantBtn;
    }

    public void setRenederedSuivantBtn(String renederedSuivantBtn) {
        this.renederedSuivantBtn = renederedSuivantBtn;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getAdminVisible() {
        return adminVisible;
    }

    public void setAdminVisible(String adminVisible) {
        this.adminVisible = adminVisible;
    }

    public String getUserVisible() {
        return userVisible;
    }

    public void setUserVisible(String userVisible) {
        this.userVisible = userVisible;
    }

    public String getVisitorVisible() {
        return visitorVisible;
    }

    public void setVisitorVisible(String visitorVisible) {
        this.visitorVisible = visitorVisible;
    }

}
