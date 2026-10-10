package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;
import madstodolist.controller.exception.UsuarioNotFoundException;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;

import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        // 1. Comprobamos si es admin (lanza excepción si no) y lo añadimos para la barra
        UsuarioData usuario = comprobarAdmin();
        model.addAttribute("usuario", usuario);

        // 2. Obtener todos los usuarios y añadirlos al modelo
        List<UsuarioData> usuarios = usuarioService.allUsuarios();
        model.addAttribute("usuarios", usuarios);

        // 3. Devolver la vista
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable(value="id") Long idUsuario, Model model) {
        // 1. Comprobamos si es admin (lanza excepción si no) y lo añadimos para la barra
        UsuarioData usuario = comprobarAdmin();
        model.addAttribute("usuario", usuario);

        // 2. Buscar usuario a consultar y lanzar excepción si no existe
        UsuarioData usuarioDescripcion = usuarioService.findById(idUsuario);
        if (usuarioDescripcion == null) {
            throw new UsuarioNotFoundException();
        }
        
        // 3. Añadir el usuario consultado al modelo
        model.addAttribute("usuarioDescripcion", usuarioDescripcion);

        // 4. Devolver la vista
        return "descripcionUsuario";
    }
    private UsuarioData comprobarAdmin() {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }
        
        UsuarioData usuario = usuarioService.findById(idUsuario);
        
        // NUEVO: Comprobamos si el usuario no existe en la BD
        if (usuario == null) {
            throw new UsuarioNoLogeadoException();
        }
        
        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }
        return usuario;
    }
}