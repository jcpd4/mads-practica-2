package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import madstodolist.controller.exception.EquipoNotFoundException;
import madstodolist.service.EquipoServiceException;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        model.addAttribute("usuario", usuario);

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);

        return "listaEquipos";
    }

    @GetMapping("/equipos/nuevo")
    public String formNuevoEquipo(@ModelAttribute EquipoData equipoData, Model model) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        model.addAttribute("usuario", usuario);
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nuevo")
    public String nuevoEquipo(@ModelAttribute EquipoData equipoData, Model model,
                              RedirectAttributes flash) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        try {
            equipoService.crearEquipo(equipoData.getNombre());
        } catch (EquipoServiceException e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", e.getMessage());
            return "formNuevoEquipo";
        }
        flash.addFlashAttribute("mensaje", "Equipo creado correctamente");
        return "redirect:/equipos";
    }


    @GetMapping("/equipos/{id}")
    public String descripcionEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        model.addAttribute("usuario", usuario);

        try {
            EquipoData equipo = equipoService.recuperarEquipo(idEquipo);
            List<UsuarioData> usuarios = equipoService.usuariosEquipo(idEquipo);
            model.addAttribute("equipo", equipo);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("perteneceAlEquipo",
                    equipoService.usuarioPerteneceAEquipo(idEquipo, usuario.getId()));

        } catch (EquipoServiceException e) {
            throw new EquipoNotFoundException();
        }

        return "descripcionEquipo";
    }

    @PostMapping("/equipos/{id}/unirse")
    public String unirseAEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        try {
            equipoService.añadirUsuarioAEquipo(idEquipo, usuario.getId());
            flash.addFlashAttribute("mensaje", "Te has unido al equipo");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/equipos/" + idEquipo;
    }

    @PostMapping("/equipos/{id}/salir")
    public String salirDeEquipo(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        UsuarioData usuario = comprobarUsuarioLogeado();
        try {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, usuario.getId());
            flash.addFlashAttribute("mensaje", "Has salido del equipo");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/equipos/" + idEquipo;
    }



    // Devuelve el usuario logeado o lanza una excepción (401) si no hay ninguno
    private UsuarioData comprobarUsuarioLogeado() {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }
        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (usuario == null) {
            throw new UsuarioNoLogeadoException();
        }
        return usuario;
    }
}
