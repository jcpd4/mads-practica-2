package madstodolist.service;

import madstodolist.dto.EquipoData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
// test 10
import static org.assertj.core.api.Assertions.assertThatThrownBy;
// test 7
import java.util.List;

// test 8
import madstodolist.dto.UsuarioData;




@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;


    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }
        @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }
        @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    }
    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
    }
    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    // test 2 jk 
        @Test
    public void eliminarUsuarioDeEquipo() {
        // GIVEN
        // Un usuario que pertenece a un equipo
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN
        // Eliminamos el usuario del equipo
        equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El equipo ya no tiene al usuario y el usuario ya no tiene el equipo
        assertThat(equipoService.usuariosEquipo(equipo.getId())).isEmpty();
        assertThat(equipoService.equiposUsuario(usuario.getId())).isEmpty();
    }

    // test 3 jk
    @Test
    public void eliminarUsuarioDeEquipoExcepciones() {
        // El equipo no existe
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);

        // GIVEN
        // Un usuario y un equipo en la BD, sin relación entre ellos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        UsuarioData usuarioBD = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // El usuario no existe
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuarioBD.getId() + 1))
                .isInstanceOf(EquipoServiceException.class);

        // El usuario existe pero no pertenece al equipo
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuarioBD.getId()))
                .isInstanceOf(EquipoServiceException.class);
    }

    // test 4 jk
    @Test
    public void crearEquipoConNombreVacioLanzaExcepcion() {
        // WHEN, THEN
        // Crear un equipo sin nombre, con nombre vacío o solo con espacios
        // lanza una excepción
        assertThatThrownBy(() -> equipoService.crearEquipo(null))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.crearEquipo(""))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.crearEquipo("   "))
                .isInstanceOf(EquipoServiceException.class);

        // THEN
        // No se ha guardado ningún equipo en la base de datos
        assertThat(equipoService.findAllOrdenadoPorNombre()).isEmpty();
    }
    // test 5 jk
    @Test
    public void añadirUsuarioQueYaPerteneceLanzaExcepcion() {
        // GIVEN
        // Un usuario que ya pertenece a un equipo
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        UsuarioData usuarioBD = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuarioBD.getId());

        // WHEN, THEN
        // Volver a añadirlo lanza una excepción
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), usuarioBD.getId()))
                .isInstanceOf(EquipoServiceException.class);

        // THEN
        // El equipo sigue teniendo un único miembro
        assertThat(equipoService.usuariosEquipo(equipo.getId())).hasSize(1);
    }


}
