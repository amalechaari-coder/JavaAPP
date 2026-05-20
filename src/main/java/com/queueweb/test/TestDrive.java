package com.queueweb.test;

import com.queueweb.DAO.AbstractDAO;
import com.queueweb.MDP.Ticket;
import com.queueweb.service.QueueManagementService;

/**
 * Petit scenario console pour verifier les operations principales.
 */
public class TestDrive {

    /**
     * Cree un patient, prend un ticket et appelle le suivant.
     */
    public static void main(String[] args) {
        QueueManagementService service = new QueueManagementService();

        service.registerPatient("etudiant", "1234");

        Ticket ticket = service.takeTicket("etudiant", "Cabinet General");
        System.out.println("Ticket cree: " + (ticket == null ? "aucun" : ticket.getNumber()));

        Ticket current = service.callNextPatient("Cabinet General");
        System.out.println("Ticket courant: " + (current == null ? "aucun" : current.getNumber()));

        AbstractDAO.closeSharedConnection();
    }
}
