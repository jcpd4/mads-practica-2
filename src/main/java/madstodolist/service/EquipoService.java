package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.model.Equipo;
import madstodolist.repository.EquipoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//test 7
import java.util.List;
import java.util.stream.Collectors;

//test 8
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;




@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;


    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        // añadiendo if para pasar el test 4 fk
        if (nombre == null || nombre.trim().isEmpty())
            throw new EquipoServiceException("El nombre del equipo no puede estar vacío");
        Equipo equipo = new Equipo(nombre);
        equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) throw new EquipoServiceException("No existe equipo con id " + id);
        return modelMapper.map(equipo, EquipoData.class);
    }
    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        List<Equipo> equipos = equipoRepository.findAllByOrderByNombreAsc();
        return equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void añadirUsuarioAEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        if (equipo == null) throw new EquipoServiceException("No existe equipo con id " + idEquipo);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) throw new EquipoServiceException("No existe usuario con id " + idUsuario);
        // test 5 jk
        if (equipo.getUsuarios().contains(usuario))
            throw new EquipoServiceException("El usuario " + idUsuario + " ya pertenece al equipo " + idEquipo);
        equipo.addUsuario(usuario);
    }

    // test 2 jk y actualizado para el test 3 jk
    @Transactional
    public void eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        if (equipo == null) throw new EquipoServiceException("No existe equipo con id " + idEquipo);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) throw new EquipoServiceException("No existe usuario con id " + idUsuario);
        if (!equipo.getUsuarios().contains(usuario))
            throw new EquipoServiceException("El usuario " + idUsuario + " no pertenece al equipo " + idEquipo);
        equipo.removeUsuario(usuario);
    }




    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long idEquipo) {
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        if (equipo == null) throw new EquipoServiceException("No existe equipo con id " + idEquipo);
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) throw new EquipoServiceException("No existe usuario con id " + idUsuario);
        return usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }

}
