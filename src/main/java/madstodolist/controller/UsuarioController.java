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

import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        // 1. Obtener todos los usuarios y añadirlos al modelo
        List<UsuarioData> usuarios = usuarioService.allUsuarios();
        model.addAttribute("usuarios", usuarios);

        // 2. Gestionar la barra de menú según si hay sesión
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario != null) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("usuario", usuario);
        }

        // 3. Devolver la vista
        return "listaUsuarios";
    }
    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable(value="id") Long idUsuario, Model model) {
        // 1 y 2. Buscar usuario y lanzar excepción si no existe
        UsuarioData usuarioDescripcion = usuarioService.findById(idUsuario);
        if (usuarioDescripcion == null) {
            throw new UsuarioNotFoundException();
        }
        
        // 3. Añadir el usuario a consultar al modelo
        model.addAttribute("usuarioDescripcion", usuarioDescripcion);

        // 4. Gestionar la barra de menú para el usuario logeado
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (idLogeado != null) {
            UsuarioData usuario = usuarioService.findById(idLogeado);
            model.addAttribute("usuario", usuario);
        }

        // 5. Devolver la vista
        return "descripcionUsuario";
    }
}