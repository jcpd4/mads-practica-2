package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EquipoService equipoService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Método auxiliar: registra un usuario y simula que está logeado
    private UsuarioData logearUsuario() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("ana@ua");
        usuario.setNombre("Ana");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());
        return usuario;
    }

    @Test
    public void listadoEquiposMuestraNombresOrdenados() throws Exception {
        // GIVEN
        // Un usuario logeado y dos equipos creados en orden no alfabético
        logearUsuario();
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN, THEN
        // El listado muestra los dos equipos ordenados alfabéticamente
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        stringContainsInOrder("Proyecto AAA", "Proyecto BBB")));
    }

    @Test
    public void listadoEquiposVacioMuestraMensaje() throws Exception {
        // GIVEN
        // Un usuario logeado y ningún equipo en la base de datos
        logearUsuario();

        // WHEN, THEN
        // Se muestra el mensaje de que no hay equipos
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No hay equipos")));
    }

    @Test
    public void listadoEquiposSinLoginDevuelve401() throws Exception {
        // GIVEN
        // Ningún usuario logeado (hay que indicarlo explícitamente:
        // Mockito devuelve 0L por defecto, no null)
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // La petición devuelve 401 Unauthorized
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isUnauthorized());
    }
}
