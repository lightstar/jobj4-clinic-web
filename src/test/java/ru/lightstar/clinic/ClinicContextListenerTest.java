package ru.lightstar.clinic;

import org.junit.Test;
import org.mockito.Mockito;
import ru.lightstar.clinic.persistence.MessageService;
import ru.lightstar.clinic.persistence.RoleService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <code>ClinicContextListener</code> class tests.
 *
 * @author LightStar
 * @since 0.0.1
 */
public class ClinicContextListenerTest extends Mockito {

    /**
     * Tests correct setting of jdbc attributes in <code>contextInitialized</code> method and
     * correctness of <code>contextDestroyed</code> after that.
     * It is integral test by nature so it will fail if there is no working database with proper schema.
     */
    @Test
    public void whenContextInitializedAndDestroyedWithJdbcThenResult() throws SQLException {
        final ServletContextEvent event = mock(ServletContextEvent.class);
        final ServletContext context = mock(ServletContext.class);
        final Connection connection = new JdbcConnectionMocker().getConnection();

        when(event.getServletContext()).thenReturn(context);
        when(context.getAttribute("jdbcConnection")).thenReturn(connection);

        final ClinicContextListener listener = new ClinicContextListener();
        listener.contextInitialized(event);

        verify(context, atLeastOnce()).setAttribute(eq("jdbcConnection"), any(Connection.class));
        verify(context, atLeastOnce()).setAttribute(eq("clinicService"), any(ClinicService.class));
        verify(context, atLeastOnce()).setAttribute(eq("drugService"), any(DrugService.class));

        listener.contextDestroyed(event);

        verify(connection, times(1)).close();
    }

    /**
     * Tests correct setting of hibernate attributes in <code>contextInitialized</code> method and
     * correctness of <code>contextDestroyed</code> after that.
     * It is integral test by nature so it will fail if there is no working database with proper schema.
     */
    @Test
    public void whenContextInitializedAndDestroyedWithHibernateThenResult() throws SQLException {
        final ServletContextEvent event = mock(ServletContextEvent.class);
        final ServletContext context = mock(ServletContext.class);

        when(event.getServletContext()).thenReturn(context);
        when(context.getInitParameter("useHibernate")).thenReturn("true");

        final ClinicContextListener listener = new ClinicContextListener();
        listener.contextInitialized(event);

        verify(context, atLeastOnce()).setAttribute(eq("clinicService"), any(ClinicService.class));
        verify(context, atLeastOnce()).setAttribute(eq("drugService"), any(DrugService.class));
        verify(context, atLeastOnce()).setAttribute(eq("roleService"), any(RoleService.class));
        verify(context, atLeastOnce()).setAttribute(eq("messageService"), any(MessageService.class));

        listener.contextDestroyed(event);
    }
}