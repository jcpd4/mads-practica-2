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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class UsuariosRegistradosWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void listadoUsuariosMuestraEmailsEIds() throws Exception {
        // GIVEN: dos usuarios registrados en la base de datos
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("ana@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("pepe@ua");
        usuario2.setPassword("123");
        usuario2 = usuarioService.registrar(usuario2);

        // WHEN, THEN: GET a /registrados devuelve el HTML con ambos emails e ids
        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(allOf(
                        containsString("ana@ua"),
                        containsString("pepe@ua"),
                        containsString(usuario1.getId().toString()),
                        containsString(usuario2.getId().toString())
                )));
    }
    @Test
    public void descripcionUsuarioMuestraDatosYNoContrasena() throws Exception {
        // GIVEN: un usuario registrado con una contraseña reconocible
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("carlos@ua");
        usuario.setNombre("Carlos");
        usuario.setPassword("claveSecreta");
        usuario = usuarioService.registrar(usuario);

        // WHEN, THEN: GET a /registrados/{id} muestra email y nombre, pero NO la contraseña
        this.mockMvc.perform(get("/registrados/" + usuario.getId()))
                .andExpect(content().string(allOf(
                        containsString("carlos@ua"),
                        containsString("Carlos"),
                        not(containsString("claveSecreta"))
                )));
    }

    @Test
    public void descripcionUsuarioNoExistenteDevuelve404() throws Exception {
        // GIVEN: la base de datos vacía (no creamos nada)

        // WHEN, THEN: GET a un ID que no existe devuelve error 404 (Not Found)
        this.mockMvc.perform(get("/registrados/999"))
                .andExpect(status().isNotFound());
    }
}