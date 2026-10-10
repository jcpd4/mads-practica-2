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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
        logearAdmin();
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("ana@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("pepe@ua");
        usuario2.setPassword("123");
        usuario2 = usuarioService.registrar(usuario2);

        // WHEN, THEN: GET a /registrados devuelve el HTML con ambos emails e ids exactos en celdas
        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(allOf(
                        containsString("ana@ua"),
                        containsString("pepe@ua"),
                        containsString("<td>" + usuario1.getId().toString() + "</td>"),
                        containsString("<td>" + usuario2.getId().toString() + "</td>")
                )));
    }

    @Test
    public void listadoUsuariosContieneEnlaceADescripcion() throws Exception {
        // GIVEN: un usuario registrado
        logearAdmin();
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("enlace@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // WHEN, THEN: el listado contiene el enlace href exacto a su perfil
        String urlEnlace = "/registrados/" + usuario.getId();
        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(containsString("href=\"" + urlEnlace + "\"")));
    }
    @Test
    public void descripcionUsuarioMuestraDatosYNoContrasena() throws Exception {
        // GIVEN: un usuario registrado con una contraseña reconocible
        logearAdmin();
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
        logearAdmin();
        // WHEN, THEN: GET a un ID que no existe devuelve error 404 (Not Found)
        this.mockMvc.perform(get("/registrados/999"))
                .andExpect(status().isNotFound());
    }
    private UsuarioData logearAdmin() {
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin_test@ua");
        admin.setPassword("123");
        admin.setNombre("Admin");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);
        
        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());
        return admin;
    }
}