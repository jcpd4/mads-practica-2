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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;

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
    @Test
    public void listadoUsuariosSinLoginDevuelve401() throws Exception {
        // GIVEN: nadie logeado (hacemos explícito que el mock devuelve null)
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN: GET a /registrados devuelve 401 Unauthorized
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void listadoUsuariosConUsuarioNormalDevuelve401() throws Exception {
        // GIVEN: un usuario normal (no admin) logeado
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setEmail("normal1@ua");
        usuarioNormal.setPassword("123");
        usuarioNormal = usuarioService.registrar(usuarioNormal);
        
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioNormal.getId());

        // WHEN, THEN: GET a /registrados devuelve 401 Unauthorized
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void descripcionUsuarioConUsuarioNormalDevuelve401() throws Exception {
        // GIVEN: un usuario normal (no admin) logeado
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setEmail("normal2@ua");
        usuarioNormal.setPassword("123");
        usuarioNormal = usuarioService.registrar(usuarioNormal);
        
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioNormal.getId());

        // WHEN, THEN: GET a /registrados/{id} devuelve 401 Unauthorized
        this.mockMvc.perform(get("/registrados/" + usuarioNormal.getId()))
                .andExpect(status().isUnauthorized());
    }
    @Test
    public void barraDeMenuMuestraEnlaceUsuariosParaAdmin() throws Exception {
        // GIVEN: un admin logeado
        logearAdmin();

        // WHEN, THEN: la barra de menú contiene el enlace a /registrados
        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(containsString("href=\"/registrados\"")));
    }

    @Test
    public void adminPuedeBloquearUsuario() throws Exception {
        // GIVEN: un admin logeado y un usuario normal registrado
        logearAdmin();
        
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setEmail("para_bloquear@ua");
        usuarioNormal.setPassword("123");
        usuarioNormal = usuarioService.registrar(usuarioNormal);

        // WHEN / THEN: POST a /registrados/{id}/bloqueo redirige al listado
        this.mockMvc.perform(post("/registrados/" + usuarioNormal.getId() + "/bloqueo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        // Y verificamos que realmente ha quedado bloqueado en la base de datos
        assertThat(usuarioService.findById(usuarioNormal.getId()).isBloqueado()).isTrue();
    }

    @Test
    public void usuarioNormalNoPuedeBloquearUsuario() throws Exception {
        // GIVEN: un usuario normal logeado y otro usuario en la BD
        UsuarioData usuarioLogeado = new UsuarioData();
        usuarioLogeado.setEmail("logueado_normal@ua");
        usuarioLogeado.setPassword("123");
        usuarioLogeado = usuarioService.registrar(usuarioLogeado);
        
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioLogeado.getId());

        UsuarioData otroUsuario = new UsuarioData();
        otroUsuario.setEmail("otro_normal@ua");
        otroUsuario.setPassword("123");
        otroUsuario = usuarioService.registrar(otroUsuario);

        // WHEN / THEN: al intentar hacer el POST, da error 401 Unauthorized
        this.mockMvc.perform(post("/registrados/" + otroUsuario.getId() + "/bloqueo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void listadoUsuariosMuestraBotonBloquear() throws Exception {
        // GIVEN: un admin logeado y un usuario normal
        logearAdmin();
        
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setEmail("normal_boton@ua");
        usuarioNormal.setPassword("123");
        usuarioNormal = usuarioService.registrar(usuarioNormal);

        // WHEN / THEN: GET a /registrados contiene el texto del botón "Bloquear"
        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(containsString("Bloquear")));
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