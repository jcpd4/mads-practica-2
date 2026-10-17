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
import madstodolist.dto.EquipoData;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

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
    @Test
    public void barraMenuContieneEnlaceAEquipos() throws Exception {
        // GIVEN
        // Un usuario logeado
        UsuarioData usuario = logearUsuario();

        // WHEN, THEN
        // La página de tareas del usuario (que usa la barra de menú común)
        // contiene el enlace a la página de equipos
        this.mockMvc.perform(get("/usuarios/" + usuario.getId() + "/tareas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/equipos\"")));
    }
    @Test
    public void descripcionEquipoMuestraSusUsuarios() throws Exception {
        // GIVEN
        // Un usuario logeado que pertenece a un equipo
        UsuarioData usuario = logearUsuario();
        EquipoData equipo = equipoService.crearEquipo("Proyecto AAA");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN, THEN
        // La página del equipo muestra su nombre y el email del miembro
        this.mockMvc.perform(get("/equipos/" + equipo.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Proyecto AAA"),
                        containsString("ana@ua")
                )));
    }

    @Test
    public void descripcionEquipoNoExistenteDevuelve404() throws Exception {
        // GIVEN
        // Un usuario logeado y ningún equipo en la BD
        logearUsuario();

        // WHEN, THEN
        // Pedir un equipo que no existe devuelve 404 Not Found
        this.mockMvc.perform(get("/equipos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listadoEquiposContieneEnlaceADescripcion() throws Exception {
        // GIVEN
        // Un usuario logeado y un equipo
        logearUsuario();
        EquipoData equipo = equipoService.crearEquipo("Proyecto AAA");

        // WHEN, THEN
        // El listado contiene el enlace a la página del equipo
        this.mockMvc.perform(get("/equipos"))
                .andExpect(content().string(
                        containsString("href=\"/equipos/" + equipo.getId() + "\"")));
    }
        @Test
    public void getNuevoEquipoDevuelveFormulario() throws Exception {
        // GIVEN
        // Un usuario logeado
        logearUsuario();

        // WHEN, THEN
        // Se muestra el formulario para crear un equipo
        this.mockMvc.perform(get("/equipos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Nuevo equipo"),
                        containsString("action=\"/equipos/nuevo\"")
                )));
    }

    @Test
    public void getNuevoEquipoSinLoginDevuelve401() throws Exception {
        // GIVEN
        // Ningún usuario logeado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        // No se puede acceder al formulario
        this.mockMvc.perform(get("/equipos/nuevo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postNuevoEquipoCreaEquipoYRedirige() throws Exception {
        // GIVEN
        // Un usuario logeado
        logearUsuario();

        // WHEN, THEN
        // Al enviar el formulario se redirige al listado con un mensaje
        this.mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Proyecto AAA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"))
                .andExpect(flash().attribute("mensaje", "Equipo creado correctamente"));

        // THEN
        // El equipo se ha guardado en la base de datos
        assertThat(equipoService.findAllOrdenadoPorNombre()).hasSize(1);
        assertThat(equipoService.findAllOrdenadoPorNombre().get(0).getNombre())
                .isEqualTo("Proyecto AAA");
    }

    @Test
    public void postNuevoEquipoConNombreVacioMuestraError() throws Exception {
        // GIVEN
        // Un usuario logeado
        logearUsuario();

        // WHEN, THEN
        // Al enviar el formulario vacío se vuelve a mostrar con un error
        this.mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El nombre del equipo no puede estar")));

        // THEN
        // No se ha creado ningún equipo
        assertThat(equipoService.findAllOrdenadoPorNombre()).isEmpty();
    }


}
