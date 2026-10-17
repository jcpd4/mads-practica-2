package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class AcercaDeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getAboutDevuelveNombreAplicacion() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("ToDoList")));
    }

    @Test
    public void getAboutSinLoginMuestraLoginYRegistro() throws Exception {
        // GIVEN
        // Ningún usuario logeado (el mock devuelve null por defecto)
        when(managerUserSession.usuarioLogeado()).thenReturn(null);
        // WHEN, THEN
        // la página muestra los enlaces a login y registro
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Login"),
                        containsString("Registro")
                )));
    }

    @Test
    public void getAboutConLoginMuestraBarraUsuario() throws Exception {
        // GIVEN
        // Un usuario con nombre registrado en la BD y logeado
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("ana@ua");
        usuario.setNombre("Ana");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        // WHEN, THEN
        // la página muestra la barra del usuario y no el enlace de registro
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Cerrar sesión Ana"),
                        not(containsString("Registro"))
                )));
    }
}