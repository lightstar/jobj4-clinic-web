package ru.lightstar.clinic.persistence.jdbc;

import ru.lightstar.clinic.exception.NameException;
import ru.lightstar.clinic.exception.ServiceException;
import ru.lightstar.clinic.model.Client;
import ru.lightstar.clinic.model.Role;
import ru.lightstar.clinic.persistence.PersistentClinicService;
import ru.lightstar.clinic.pet.Pet;
import ru.lightstar.clinic.pet.Sex;

import java.sql.*;

/**
 * Database-aware <code>ClinicService</code> implementation which uses JDBC.
 *
 * @author LightStar
 * @since 0.0.1
 */
public class JdbcClinicService extends PersistentClinicService {

    /**
     * SQL used to load all client data from database.
     */
    public static final String LOAD_SQL =
            "SELECT client.id, client.position, client.name, client.email, client.phone, client.password, " +
                    "role.id AS roleId, role.name AS roleName, " +
                    "pet.id AS petId, pet.type AS petType, pet.name AS petName, pet.age AS petAge," +
                    "pet.sex AS petSex " +
                    "FROM client " +
                    "INNER JOIN role ON client.role_id = role.id " +
                    "LEFT JOIN pet ON client.id = pet.client_id";

    /**
     * SQL used to add new client to database.
     */
    public static final String ADD_CLIENT_SQL = "INSERT INTO client " +
            "(`position`, `name`,`email`,`phone`,`role_id`,`password`) VALUES (?,?,?,?,?,?)";

    /**
     * SQL used to set client's pet in database.
     */
    public static final String SET_CLIENT_PET_SQL = "REPLACE INTO pet " +
            "(`client_id`, `type`, `name`,`age`,`sex`) VALUES (?,?,?,?,?)";

    /**
     * SQL used to update client in database.
     */
    public static final String UPDATE_CLIENT_SQL = "UPDATE client " +
            "SET name = ?, email = ?, phone = ?, role_id = ?, password = ? " +
            "WHERE name = ?";

    /**
     * SQL used to update client's pet in database.
     */
    public static final String UPDATE_CLIENT_PET_SQL = "UPDATE pet SET name = ?, age = ?, sex = ? " +
            "WHERE client_id = ?";

    /**
     * SQL used to delete client's pet from database.
     */
    public static final String DELETE_CLIENT_PET_SQL = "DELETE FROM pet WHERE client_id = ?";

    /**
     * SQL used to delete client from database.
     * All his pets will be deleted automatically due to foreign key constraint.
     */
    public static final String DELETE_CLIENT_SQL = "DELETE FROM client WHERE id = ?";

    /**
     * Jdbc connection used by this service.
     */
    private final Connection connection;

    /**
     * Constructs <code>JdbcClinicService</code> object.
     *
     * @param connection jdbc connection.
     */
    public JdbcClinicService(final Connection connection) {
        super();
        this.connection = connection;
        this.loadClinic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Client addClient(final int position, final String name, final String email,
                                         final String phone, final Role role, final String password)
            throws ServiceException, NameException {
        final Client client = super.addClient(position, name, email, phone, role, password);

        try (final PreparedStatement statement = this.connection.prepareStatement(ADD_CLIENT_SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, client.getPosition());
            statement.setString(2, client.getName());
            statement.setString(3, client.getEmail());
            statement.setString(4, client.getPhone());
            statement.setInt(5, client.getRole().getId());
            statement.setString(6, client.getPassword());
            statement.executeUpdate();

            try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    client.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            this.undoAddClient(client);
            throw new IllegalStateException(String.format("Can't insert client into database: %s",
                    e.getMessage()));
        }

        return client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Pet setClientPet(final String name, final String petType, final String petName,
                                         final int petAge, final Sex petSex)
            throws ServiceException, NameException {
        final Client client = this.findClientByName(name);
        final Pet oldPet = client.getPet();
        final Pet pet = super.setClientPet(client, petType, petName, petAge, petSex);

        try (final PreparedStatement statement = this.connection.prepareStatement(SET_CLIENT_PET_SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, client.getId());
            statement.setString(2, pet.getType());
            statement.setString(3, pet.getName());
            statement.setInt(4, pet.getAge());
            statement.setString(5, pet.getSex() == Sex.M ? "m" : "f");
            statement.executeUpdate();

            try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pet.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            this.undoSetClientPet(client, oldPet);
            throw new IllegalStateException(String.format("Can't insert client's pet into database: %s",
                    e.getMessage()));
        }

        return pet;
    }

    /**
     * {@inheritDoc}
     */
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

        try (final PreparedStatement statement = this.connection.prepareStatement(UPDATE_CLIENT_SQL)) {
            statement.setString(1, newName);
            statement.setString(2, newEmail);
            statement.setString(3, newPhone);
            statement.setInt(4, newRole.getId());
            statement.setString(5, newPassword);
            statement.setString(6, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            this.undoUpdateClient(client, name, oldEmail, oldPhone, oldRole, oldPassword);
            throw new IllegalStateException(String.format("Can't update client's name in database: %s",
                    e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
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

        try (final PreparedStatement statement = this.connection.prepareStatement(UPDATE_CLIENT_PET_SQL)) {
            statement.setString(1, petName);
            statement.setInt(2, petAge);
            statement.setString(3, petSex == Sex.M ? "m" : "f");
            statement.setInt(4, client.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.undoUpdateClientPet(client, oldPetName, oldPetAge, oldPetSex);
            throw new IllegalStateException(String.format("Can't update client pet's name in database: %s",
                    e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deleteClientPet(final String name) throws ServiceException {
        final Client client = this.findClientByName(name);
        final Pet oldPet = client.getPet();
        super.deleteClientPet(client);

        try (final PreparedStatement statement = this.connection.prepareStatement(DELETE_CLIENT_PET_SQL)) {
            statement.setInt(1, client.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.undoDeleteClientPet(client, oldPet);
            throw new IllegalStateException(String.format("Can't delete client's pet from database: %s",
                    e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deleteClient(final String name) throws ServiceException {
        final Client client = this.findClientByName(name);
        super.deleteClient(client);

        try (final PreparedStatement statement = this.connection.prepareStatement(DELETE_CLIENT_SQL)) {
            statement.setInt(1, client.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            this.undoDeleteClient(client);
            throw new IllegalStateException(String.format("Can't delete client from database: %s",
                    e.getMessage()));
        }
    }

    /**
     * Load all data from database to inner clinic object.
     */
    private synchronized void loadClinic() {
        try (final Statement statement = this.connection.createStatement()) {
            try (final ResultSet rs = statement.executeQuery(LOAD_SQL)) {
                while (rs.next()) {
                    final int id = rs.getInt("id");
                    final int position = rs.getInt("position");
                    final String name = rs.getString("name");
                    final String email = rs.getString("email");
                    final String phone = rs.getString("phone");
                    final String password = rs.getString("password");
                    final int petId = rs.getInt("petId");
                    final String petType = rs.getString("petType");
                    final String petName = rs.getString("petName");
                    final int petAge = rs.getInt("petAge");
                    final String petSex = rs.getString("petSex");
                    final int roleId = rs.getInt("roleId");
                    final String roleName = rs.getString("roleName");

                    final Role role = new Role(roleName);
                    role.setId(roleId);
                    final Client client = super.addClient(position, name, email, phone, role, password);
                    client.setId(id);
                    if (petType != null) {
                        final Pet pet = super.setClientPet(client, petType, petName, petAge,
                                petSex.toLowerCase().equals("m") ? Sex.M : Sex.F);
                        pet.setId(petId);
                    }
                }
            }
        } catch (SQLException | ServiceException | NameException e) {
            throw new IllegalStateException("Can't load data from database", e);
        }
    }
}
