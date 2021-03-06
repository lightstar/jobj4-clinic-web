package ru.lightstar.clinic.persistence.hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lightstar.clinic.exception.NameException;
import ru.lightstar.clinic.exception.ServiceException;
import ru.lightstar.clinic.model.Client;
import ru.lightstar.clinic.model.Role;
import ru.lightstar.clinic.persistence.PersistentClinicService;
import ru.lightstar.clinic.pet.Pet;
import ru.lightstar.clinic.pet.Sex;

import java.util.List;

/**
 * Database-aware <code>ClinicService</code> implementation which uses hibernate.
 *
 * @author LightStar
 * @since 0.0.1
 */
@Service
public class HibernateClinicService extends PersistentClinicService {

    /**
     * Spring's hibernate template.
     */
    private final HibernateTemplate hibernateTemplate;

    /**
     * Constructs <code>HibernateClinicService</code> object.
     *
     * @param hibernateTemplate spring's hibernate template.
     */
    @Autowired
    public HibernateClinicService(final HibernateTemplate hibernateTemplate) {
        super();
        this.hibernateTemplate = hibernateTemplate;
        this.loadClinic();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized Client addClient(final int position, final String name, final String email,
                                         final String phone, final Role role, final String password)
            throws ServiceException, NameException {
        final Client client = super.addClient(position, name, email, phone, role, password);

        try {
            this.hibernateTemplate.save(client);
        } catch (DataAccessException e) {
            this.undoAddClient(client);
            throw e;
        }

        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized Pet setClientPet(final String name, final String petType, final String petName,
                                         final int petAge, final Sex petSex)
            throws ServiceException, NameException {
        final Client client = this.findClientByName(name);
        final Pet oldPet = client.getPet();
        final Pet pet = super.setClientPet(client, petType, petName, petAge, petSex);

        try {
            this.hibernateTemplate.saveOrUpdate(pet);
        } catch (DataAccessException e) {
            this.undoSetClientPet(client, oldPet);
            throw e;
        }

        return pet;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized void updateClient(final String name, final String newName,
                                          final String newEmail, final String newPhone,
                                          final Role newRole, String newPassword)
            throws ServiceException, NameException {
        final Client client = this.findClientByName(name);
        final String oldEmail = client.getEmail();
        final String oldPhone = client.getPhone();
        final Role oldRole = client.getRole();
        final String oldPassword = client.getPassword();
        if (newPassword.isEmpty()) {
            newPassword = oldPassword;
        }
        super.updateClient(client, newName, newEmail, newPhone, newRole, newPassword);

        try {
            this.hibernateTemplate.update(client);
        } catch (DataAccessException e) {
            this.undoUpdateClient(client, name, oldEmail, oldPhone, oldRole, oldPassword);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized void updateClientPet(final String name, final String petName,
                                             final int petAge, final Sex petSex)
            throws ServiceException, NameException {
        final Client client = this.findClientByName(name);
        final Pet pet = client.getPet();
        final String oldPetName = pet.getName();
        final int oldPetAge = pet.getAge();
        final Sex oldPetSex = pet.getSex();
        super.updateClientPet(client, petName, petAge, petSex);

        try {
            this.hibernateTemplate.update(pet);
        } catch (DataAccessException e) {
            this.undoUpdateClientPet(client, oldPetName, oldPetAge, oldPetSex);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized void deleteClientPet(final String name) throws ServiceException {
        final Client client = this.findClientByName(name);
        final Pet pet = client.getPet();
        super.deleteClientPet(client);

        try {
            this.hibernateTemplate.delete(pet);
        } catch (DataAccessException e) {
            this.undoDeleteClientPet(client, pet);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public synchronized void deleteClient(final String name) throws ServiceException {
        final Client client = this.findClientByName(name);
        super.deleteClient(client);

        try {
            this.hibernateTemplate.delete(client);
        } catch (DataAccessException e) {
            this.undoDeleteClient(client);
            throw e;
        }
    }

    /**
     * Load all data from database to inner clinic object.
     */
    @SuppressWarnings("unchecked")
    private synchronized void loadClinic() {
        try {
            final List<Client> clients = (List<Client>) hibernateTemplate.find("from Client");
            for (final Client client : clients) {
                final Pet pet = client.getPet();
                client.setPet(Pet.NONE);

                super.addClient(client.getPosition(), client);
                if (pet != null) {
                    super.setClientPet(client, pet);
                }
            }
        } catch (ServiceException | NameException e) {
            throw new NonTransientDataAccessResourceException("Wrong client data in database", e);
        }
    }
}
