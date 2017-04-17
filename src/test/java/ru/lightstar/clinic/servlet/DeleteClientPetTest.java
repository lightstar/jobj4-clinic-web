package ru.lightstar.clinic.servlet;

import org.junit.Test;
import org.mockito.Mockito;
import ru.lightstar.clinic.ClinicService;
import ru.lightstar.clinic.exception.NameException;
import ru.lightstar.clinic.exception.ServiceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * <code>DeleteClientPet</code> servlet tests.
 *
 * @author LightStar
 * @since 0.0.1
 */
public class DeleteClientPetTest extends Mockito {

    /**
     * Test deleting client's pet with correct parameters in servlet.
     */
    @Test
    public void whenDoPostThenResult()
            throws ServletException, IOException, NameException, ServiceException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ClinicService clinicService = mock(ClinicService.class);
        final HttpSession session = mock(HttpSession.class);

        when(request.getParameter("name")).thenReturn("Vasya");
        when(request.getContextPath()).thenReturn("/context");
        when(request.getSession()).thenReturn(session);

        new DeleteClientPet(clinicService).doPost(request, response);

        verify(session, atLeastOnce()).setAttribute(eq("message"), anyString());
        verify(clinicService, times(1)).deleteClientPet("Vasya");
        verify(response, atLeastOnce()).sendRedirect("/context/");
    }

    /**
     * Test deleting client's pet with null parameters in servlet.
     */
    @Test
    public void whenDoPostWithNullParametersThenError()
            throws ServletException, IOException, NameException, ServiceException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ClinicService clinicService = mock(ClinicService.class);
        final HttpSession session = mock(HttpSession.class);

        when(request.getContextPath()).thenReturn("/context");
        when(request.getSession()).thenReturn(session);

        new DeleteClientPet(clinicService).doPost(request, response);

        verify(session, atLeastOnce()).setAttribute(eq("error"), anyString());
        verify(response, atLeastOnce()).sendRedirect("/context/");
    }

    /**
     * Test deleting client's pet when clinic service throws exception.
     */
    @Test
    public void whenDoPostWithServiceExceptionThenError()
            throws ServletException, IOException, NameException, ServiceException {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final ClinicService clinicService = mock(ClinicService.class);
        final HttpSession session = mock(HttpSession.class);

        when(request.getParameter("name")).thenReturn("Vasya");
        when(request.getContextPath()).thenReturn("/context");
        when(request.getSession()).thenReturn(session);
        doThrow(new ServiceException("Test error")).when(clinicService).deleteClientPet("Vasya");

        new DeleteClientPet(clinicService).doPost(request, response);

        verify(session, atLeastOnce()).setAttribute("error", "Test error.");
        verify(response, atLeastOnce()).sendRedirect("/context/");
    }
}
